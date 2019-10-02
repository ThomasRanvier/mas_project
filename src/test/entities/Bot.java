package test.entities;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import test.Main;
import test.Node;
import test.Utils;
import test.World;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Bot extends Agent {
    private int[][] innerMap = new int[Main.mapH][Main.mapW];
    public int x = Main.spaceshipX;
    public int y = Main.spaceshipY;
    public boolean deathFlag = false;
    private boolean visualisation = true;
    private boolean holdsStone = false;
    private World world;
    private long totalMoves;

    @Override
    protected void setup(){
        Object[] args = this.getArguments();
        if (args != null && args.length > 0) {
            this.world = (World) args[0];
        } else {
            System.err.println("Impossible to create bot if world is not set");
            this.doDelete();
        }
        System.out.println("Hi, I'm a little bot, " + this.getLocalName());
        this.totalMoves = 0;
        this.world.registerBots(this);
        this.initialiseInnerMap();
        this.live();
    }

    @Override
    protected void takeDown() {
        System.out.println("Taking down " + this.getLocalName());
        super.takeDown();
    }

    private void live() {
        //The life cycle of the robot
        while (!this.deathFlag) {
            if(this.visualisation){
                try {TimeUnit.MILLISECONDS.sleep(Main.visualisationsStep);}
                catch (InterruptedException e) {e.printStackTrace();}
            }
            this.updateInnerMap();
            this.tryShareMapWithSpaceship();
            this.move();
        }
        this.doDelete();
    }

    private void move() {
        if (this.deathFlag) {return;}
        if (this.holdsStone) {
            //Holds a stone, go back to the spaceship
            this.goTo(Main.spaceshipX, Main.spaceshipY);
        } else {
            int[] closestStoneCoords = this.getClosestStone();
            int closestStoneX = closestStoneCoords[0];
            int closestStoneY = closestStoneCoords[1];

            if (closestStoneX >= 0) {
                //Stone detected
                if (this.x == closestStoneX && this.y == closestStoneY) {
                    if (this.world.takeStone(closestStoneX, closestStoneY, this.getLocalName())) {
                        this.holdsStone = true;
                    }
                } else {
                    this.goTo(closestStoneX, closestStoneY);
                }
            } else {
                //No stone detected
                Node unknownCell = this.getRandomUnknownCell();
                if (unknownCell == null) {
                    this.goTo(Main.spaceshipX, Main.spaceshipY);
                } else {
                    if (unknownCell.x >= 0) {
                        this.goTo(unknownCell.x, unknownCell.y);
                    }
                }
            }
        }
    }

    private Node getRandomUnknownCell() {
        ArrayList<Node> unknownCells = new ArrayList<>();
        for (int x = 0; x < Main.mapW; x++) {
            for (int y = 0; y < Main.mapH; y++) {
                if (this.innerMap[x][y] == Main.unknownCell) {
                    unknownCells.add(new Node(x, y));
                }
            }
        }
        if (unknownCells.size() > 0) {
            Collections.shuffle(unknownCells, new Random());
            return unknownCells.get(0);
        } else {
            return null;
        }
    }

    private void tryShareMapWithSpaceship() {
        if (this.x == Main.spaceshipX && this.y == Main.spaceshipY) {
            if (this.holdsStone) {
                this.releaseStone();
            }
            if (Main.communicationActivated) {
                send(Utils.shareMap(this.getLocalName(), Main.spaceshipName, Utils.mapToString(this.innerMap)));
                //Wait for response
                ACLMessage msg = receive();
                while (msg == null) {
                    if (this.deathFlag) {this.doDelete();}
                    msg = receive();
                }
                //Message received
                //Update inner map
                String[] infos = msg.getContent().split(":");
                if (infos[1].equals("map")) {
                    this.innerMap = Utils.stringToMap(infos[2]);
                } else {
                    System.err.println("Weird msg : " + msg.getContent());
                }
            }
        }
    }

    private void setVisualisationFlag(boolean f) {
        this.visualisation = f;
    }

    private void releaseStone() {
        //Sends a message to the spaceship to inform that this bot brought back a stone
        if (this.holdsStone) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID(Main.spaceshipName,AID.ISLOCALNAME));
            msg.setLanguage("English");
            msg.setContent(this.getLocalName() + ":release:" + this.totalMoves);
            this.totalMoves = 0;
            send(msg);

            this.holdsStone = false;
        } else {
            System.err.println("Not supposed to call this function, releaseStone, " + this.getLocalName());
        }
    }

    private void goTo(int goalX, int goalY) {
        //System.out.println(this.getLocalName() + " " + this.x + ", " + this.y + " " + goalX + ", " + goalY);
        while (this.x != goalX || this.y != goalY) {
            if (this.deathFlag) {return;}
            List<Node> path = this.aStar(new Node(this.x, this.y), new Node(goalX, goalY));
            //System.out.println(path);
            if (path != null) {
                for (Node node : path) {
                    if (this.innerMap[node.x][node.y] == Main.unknownCell || this.innerMap[node.x][node.y] == Main.obstacleCell) {
                        break;
                    } else {
                        this.x = node.x;
                        this.y = node.y;
                        this.totalMoves++;
                        if(this.visualisation){
                            try {TimeUnit.MILLISECONDS.sleep(Main.visualisationsStep);}
                            catch (InterruptedException e) {e.printStackTrace();}
                        }
                        this.updateInnerMap();
                    }
                }
            } else {
                System.err.println("Careful, empty path to go to " + goalX + ", " + goalY);
            }
            if (this.innerMap[goalX][goalY] == Main.obstacleCell) {
                return;
            }
        }
    }

    private List<Node> aStar(Node start, Node goal) {
        LinkedList<Node> openList = new LinkedList<Node>();
        LinkedList<Node> closedList = new LinkedList<Node>();
        openList.add(start); // add starting node to open list

        Map<Node, Node> cameFrom = new HashMap<Node, Node>();

        Map<Node, Double> gScore = new HashMap<Node, Double>();
        gScore.put(start, 0.0);

        Map<Node, Double> fScore = new HashMap<Node, Double>();
        fScore.put(start, start.calculateDistance2(goal));

        Node current;
        while (!openList.isEmpty()) {
            current = Utils.nodeWithLowerCost(openList, fScore); // get node with lowest fCosts from openList
            if (current.equals(goal)) {
                return Utils.reconstructPath(cameFrom, current);
            }

            openList.remove(current);
            closedList.add(current);
            for (Node neighbour : Utils.getNeighbours(current)) {
                if (closedList.contains(neighbour)) {
                    continue;
                }
                double tentativeGScore = gScore.get(current) + current.calculateDistance2(neighbour);
                if (this.innerMap[neighbour.x][neighbour.y] == Main.obstacleCell) {
                    tentativeGScore = Double.POSITIVE_INFINITY;
                }
                if (this.innerMap[neighbour.x][neighbour.y] == Main.unknownCell) {
                    tentativeGScore += 10;
                }
                if (!gScore.containsKey(neighbour)) {
                    gScore.put(neighbour, Double.POSITIVE_INFINITY);
                }
                if (tentativeGScore < gScore.get(neighbour)) {
                    cameFrom.put(neighbour, current);
                    gScore.put(neighbour, tentativeGScore);
                    fScore.put(neighbour, gScore.get(neighbour) + neighbour.calculateDistance2(goal));
                    if (!openList.contains(neighbour)) {
                        openList.add(neighbour);
                    }
                }
            }
        }
        System.err.println("A* : unreachable");
        return null; // unreachable
    }

    private void updateInnerMap() {
        //Update the map by registering the visible cells
        String cells;
        cells = this.world.getCellsAround(this.x, this.y);
        if (cells.length() > 0) {
            for (String cell : cells.split(";")) {
                if (cell.length() > 0) {
                    String[] c = cell.split(",");
                    this.innerMap[Integer.parseInt(c[0])][Integer.parseInt(c[1])] = Integer.parseInt(c[2]);
                }
            }
        }
    }

    private int[] getClosestStone() {
        int[] coords = {-1, -1};
        double minDist = Double.POSITIVE_INFINITY;
        for (int x = 0; x < Main.mapW; x++) {
            for (int y = 0; y < Main.mapH; y++) {
                if (this.innerMap[x][y] > 0) {
                    double newDist = Utils.calculateDistance(this.x, this.y, x, y);
                    if (coords[0] == -1) {
                        coords[0] = x;
                        coords[1] = y;
                        minDist = newDist;
                    } else if (newDist < minDist) {
                        coords[0] = x;
                        coords[1] = y;
                        minDist = newDist;
                    }
                }
            }
        }
        return coords;
    }

    private void initialiseInnerMap() {
        for (int x = 0; x < Main.mapW; x++) {
            for (int y = 0; y < Main.mapH; y++) {
                if (x == Main.spaceshipX && y == Main.spaceshipY) {
                    this.innerMap[x][y] = Main.spaceshipCell;
                } else {
                    this.innerMap[x][y] = Main.unknownCell;
                }
            }
        }
    }

    public int getBotX(){
        return this.x;
    }

    public int getBotY(){
        return this.y;
    }

    public int[][] getInnerMap(){return this.innerMap;}

}
