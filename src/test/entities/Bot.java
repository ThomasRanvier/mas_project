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

/**
 * Bot class that extends the Jade Agent class
 */
public class Bot extends Agent {
    private int[][] innerMap = new int[Main.mapH][Main.mapW];
    public int x = Main.spaceshipX;
    public int y = Main.spaceshipY;
    private boolean visualisation = true;
    private boolean holdsStone = false;
    private World world;
    private long totalMoves;
    private int lastDx = 0;//Used to make the roaming smarter
    private int lastDy = 0;

    @Override
    protected void setup(){
        Object[] args = this.getArguments();
        if (args != null && args.length > 0) {
            this.world = (World) args[0];
        } else {
            System.err.println("Impossible to create bot if world is not set");
            this.doDelete();
        }
        System.out.println("Hi, I'm a bot, " + this.getLocalName());
        this.totalMoves = 0;
        this.world.registerBots(this);
        this.initialiseInnerMap();
        try {
            TimeUnit.MILLISECONDS.sleep(Main.initialStep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.live();
    }

    @Override
    protected void takeDown() {
        System.out.println("Taking down " + this.getLocalName());
        super.takeDown();
    }

    /**
     * Life cycle of the robot
     */
    private void live() {
        while (this.isAlive()) {
            if(this.visualisation){
                try {TimeUnit.MILLISECONDS.sleep(Main.visualisationsStep);}
                catch (InterruptedException e) {e.printStackTrace();}
            }
            this.updateInnerMap();
            this.tryShareMapWithSpaceship();
            this.move();
            this.tryReceiveMapFromSpaceship();
        }
    }

    /**
     * Used to merge map with the map of other bots that is received through the spaceship
     */
    private void tryReceiveMapFromSpaceship() {
        String mapToMerge = "";
        ACLMessage msg = receive();
        while (msg != null) {
            String[] infos = msg.getContent().split(":");
            mapToMerge = infos[2];
            msg = receive();
        }
        if (mapToMerge.length() > 0) {
            this.innerMap = Utils.mergeMaps(this.innerMap, mapToMerge);
        }
    }

    /**
     * Move action:
     * - If the bot hold a stone, it goes back to the spaceship
     * - Otherwise it searches for the closest stone and goes for it
     * - If it doesn't find a stone it goes for an unknown area or roams randomly
     */
    private void move() {
        if (this.holdsStone) {
            //Holds a stone, go back to the spaceship
            this.goTo(Main.spaceshipX, Main.spaceshipY);
        } else {
            Node closestStone = this.getClosestStone();
            if (closestStone.x >= 0) {
                //Stone detected
                if (this.x == closestStone.x && this.y == closestStone.y) {
                    boolean[] result = this.world.takeStone(closestStone);
                    if (result[0]) {
                        this.holdsStone = true;
                    }
                    if (Main.communicationActivated && Main.interBotCommunication && result[1]) {
                        //Share map with spaceship, that will send it to all the bots since new stone discovered
                        send(Utils.shareMap(this.getLocalName(), Main.spaceshipName, Utils.mapToString(this.innerMap)));
                    }
                } else {
                    this.goTo(closestStone.x, closestStone.y);
                }
            } else {
                //No stone detected
                if (Main.localGoalActivated) {
                    Node unknownCell = this.getRandomUnknownCell();
                    if (unknownCell == null) {
                        this.goTo(Main.spaceshipX, Main.spaceshipY);
                    } else {
                        if (unknownCell.x >= 0) {
                            this.goTo(unknownCell.x, unknownCell.y);
                        }
                    }
                } else {
                    this.roamAround();
                }
            }
        }
    }

    /**
     * Roaming action, based on random
     */
    private void roamAround() {
        //Randomly roam around
        Random randomiser = new Random();
        if (randomiser.nextInt(3) == 0) {//1 chance out of 3 to not change its orientation, makes roaming more fluid
            int newX = this.x + lastDx;
            int newY = this.y + lastDy;
            if (Utils.isInBoundaries(newX, newY)) {
                if (this.innerMap[newX][newY] != Main.obstacleCell) {
                    this.x += this.lastDx;
                    this.y += this.lastDy;
                }
            }
        } else {
            int dx = randomiser.nextInt(3) - 1;
            int dy = randomiser.nextInt(3) - 1;
            int newX = this.x + dx;
            int newY = this.y + dy;
            if (Utils.isInBoundaries(newX, newY)) {
                if (this.innerMap[newX][newY] != Main.obstacleCell) {
                    this.x += dx;
                    this.lastDx = dx;
                    this.y += dy;
                    this.lastDy = dy;
                }
            }
        }
        if(this.visualisation){
            try {TimeUnit.MILLISECONDS.sleep(Main.visualisationsStep);}
            catch (InterruptedException e) {e.printStackTrace();}
        }
        this.totalMoves++;
    }

    /**
     * Gives a random unknown cell
     * @return Node
     */
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

    /**
     * Shares the inner map with the spaceship using Jade messages, if communication is activated
     */
    private void tryShareMapWithSpaceship() {
        if (this.x == Main.spaceshipX && this.y == Main.spaceshipY) {
            if (this.holdsStone) {
                this.releaseStone();
            }
            if (Main.communicationActivated && !Main.interBotCommunication) {
                send(Utils.shareMap(this.getLocalName(), Main.spaceshipName, Utils.mapToString(this.innerMap)));
                //Wait for response
                ACLMessage msg = receive();
                while (msg == null) {
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

    /**
     * Sends a message to the spaceship to inform that this bot brought back a stone
     */
    private void releaseStone() {
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

    /**
     * Used to make the bot follow a path to the goal
     * @param goalX x coordinate of the goal
     * @param goalY y coordiante of the goal
     */
    private void goTo(int goalX, int goalY) {
        while (this.x != goalX || this.y != goalY) {
            List<Node> path = this.aStar(new Node(this.x, this.y), new Node(goalX, goalY));
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

    /**
     * Implementation of the a* algorithm, used to find a path to a goal
     * @param start Start point
     * @param goal Goal point
     * @return List<Node> The path under the form of a list of Node Objects
     */
    private List<Node> aStar(Node start, Node goal) {
        LinkedList<Node> openList = new LinkedList<Node>();
        LinkedList<Node> closedList = new LinkedList<Node>();
        openList.add(start); // add starting node to open list

        Map<Node, Node> cameFrom = new HashMap<Node, Node>();

        Map<Node, Double> gScore = new HashMap<Node, Double>();
        gScore.put(start, 0.0);

        Map<Node, Double> fScore = new HashMap<Node, Double>();
        fScore.put(start, Utils.calculateDistance(start.x, start.y, goal.x, goal.y));

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
                double tentativeGScore = gScore.get(current) + Utils.calculateDistance(current.x, current.y, neighbour.x, neighbour.y);
                if (this.innerMap[neighbour.x][neighbour.y] == Main.obstacleCell) {
                    tentativeGScore = Double.POSITIVE_INFINITY;
                }
                if (this.innerMap[neighbour.x][neighbour.y] >= 0) {
                    tentativeGScore += 10;
                }
                if (this.innerMap[neighbour.x][neighbour.y] == Main.unknownCell) {
                    tentativeGScore += 1;
                }
                if (!gScore.containsKey(neighbour)) {
                    gScore.put(neighbour, Double.POSITIVE_INFINITY);
                }
                if (tentativeGScore < gScore.get(neighbour)) {
                    cameFrom.put(neighbour, current);
                    gScore.put(neighbour, tentativeGScore);
                    fScore.put(neighbour, gScore.get(neighbour) + Utils.calculateDistance(goal.x, goal.y, neighbour.x, neighbour.y));
                    if (!openList.contains(neighbour)) {
                        openList.add(neighbour);
                    }
                }
            }
        }
        System.err.println("A* : unreachable");
        return null;
    }

    /**
     * Update the map by registering the visible cells
     */
    private void updateInnerMap() {
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

    /**
     * @return A Node Object that corresponds to the closest stone on the inner map of the bot
     */
    private Node getClosestStone() {
        Node coords = new Node(-1, -1);
        double minDist = Double.POSITIVE_INFINITY;
        for (int x = 0; x < Main.mapW; x++) {
            for (int y = 0; y < Main.mapH; y++) {
                if (this.innerMap[x][y] > 0) {
                    double newDist = Utils.calculateDistance(this.x, this.y, x, y);
                    if (coords.x == -1) {
                        coords.x = x;
                        coords.y = y;
                        minDist = newDist;
                    } else if (newDist < minDist) {
                        coords.x = x;
                        coords.y = y;
                        minDist = newDist;
                    }
                }
            }
        }
        return coords;
    }

    /**
     * Initialise the inner map of the bot
     */
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
