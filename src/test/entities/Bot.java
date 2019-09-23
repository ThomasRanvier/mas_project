package test.entities;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import test.Main;
import test.Node;
import test.Utils;
import test.World;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Bot extends Agent {
    private int[][] innerMap = new int[Main.mapHeight][Main.mapWidth];
    public int x = Main.spaceshipX;
    public int y = Main.spaceshipY;
    private int lastDx = 0;//Used to make the roaming smarter
    private int lastDy = 0;
    private int visualisationStep = 0;
    private boolean holdsStone = false;
    private World world;

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
        while (true) {
            this.visualisation(false);
            this.updateInnerMap();
            this.tryShareMapWithSpaceship();
            this.move();
        }
    }

    private void move() {
        if (this.holdsStone) {
            //Holds a stone, go back to the spaceship
            this.goTo(Main.spaceshipX, Main.spaceshipY);
        } else {
            int[] closestStoneCoords = this.getClosestStone();
            int closestStoneX = closestStoneCoords[1];
            int closestStoneY = closestStoneCoords[0];

            if (closestStoneX > 0) {
                //Stone detected
                System.out.println("Stone detected : " + closestStoneX + ", " + closestStoneY);
                if (this.x == closestStoneX && this.y == closestStoneY) {
                    if (this.world.takeStone(closestStoneX, closestStoneY, this.getLocalName())) {
                        this.holdsStone = true;
                    }
                } else {
                    System.out.println("Goto");
                    this.goTo(closestStoneX, closestStoneY);
                }
            } else {
                //No stone detected
                this.roamAround();
            }
        }
    }

    private void tryShareMapWithSpaceship() {
        if (this.x == Main.spaceshipX && this.y == Main.spaceshipY) {
            if (this.holdsStone) {
                this.releaseStone();
            }
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

    private void visualisation(boolean force) {
        if (Main.visualiseBotMap && this.getLocalName().equals("bot_1")) {
            this.visualisationStep++;
            if (visualisationStep >= Main.visualisationsSteps || force) {
                this.visualisationStep = 0;
                this.writeMap();
            }
        }
    }

    private void releaseStone() {
        //Sends a message to the spaceship to inform that this bot brought back a stone
        if (this.holdsStone) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID(Main.spaceshipName,AID.ISLOCALNAME));
            msg.setLanguage("English");
            msg.setContent(this.getLocalName() + ":release:1");
            send(msg);

            this.holdsStone = false;
        } else {
            System.err.println("Not supposed to call this function, releaseStone, " + this.getLocalName());
        }
    }

    private void writeMap() {
        String line = Utils.mapToString(this.innerMap) + "\n";

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(Main.botMapFile, true));
            writer.append(line);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void roamAround() {
        //Randomly roam around
        Random randomiser = new Random();
        if (randomiser.nextInt(3) == 0) {//1 chance out of 3 to not change its orientation, makes roaming more fluid
            int newX = this.x + lastDx;
            int newY = this.y + lastDy;
            if (Utils.isInBoundaries(newX, newY)) {
                if (this.innerMap[newY][newX] != Main.obstacleCell) {
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
                if (this.innerMap[newY][newX] != Main.obstacleCell) {
                    this.x += dx;
                    this.lastDx = dx;
                    this.y += dy;
                    this.lastDy = dy;
                }
            }
        }
    }

    private void goTo(int goalX, int goalY) {
        //Path finding to the coords
        List<Node> path = this.aStar(new Node(this.x, this.y), new Node(goalX, goalY));
        for (Node node : path) {
            this.x = node.x;
            this.y = node.y;
            this.updateInnerMap();
            //Check if sees another bot to merge maps
        }
        if (this.x == Main.spaceshipX && this.y == Main.spaceshipY) {
            System.out.println(this.getLocalName() + " is home");
            while (true) {

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
            if (current == goal) {
                return Utils.reconstructPath(cameFrom, current);
            }

            openList.remove(current);
            closedList.add(current);
            for (Node neighbour : Utils.getNeighbours(current)) {
                if (closedList.contains(neighbour)) {
                    continue;
                }
                double tentativeGScore = gScore.get(current) + current.calculateDistance2(neighbour);
                if (this.innerMap[neighbour.y][neighbour.x] == Main.unknownCell || this.innerMap[neighbour.y][neighbour.x] == Main.obstacleCell) {
                    tentativeGScore = Double.POSITIVE_INFINITY;
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
        for (int y = 0; y < Main.mapHeight; y++) {
            for (int x = 0; x < Main.mapWidth; x++) {
                if (this.innerMap[y][x] > 0) {
                    coords[0] = y;
                    coords[1] = x;
                }
            }
        }
        return coords;
    }

    private void initialiseInnerMap() {
        for (int y = 0; y < Main.mapHeight; y++) {
            for (int x = 0; x < Main.mapWidth; x++) {
                if (x == Main.spaceshipX && y == Main.spaceshipY) {
                    this.innerMap[y][x] = Main.spaceshipCell;
                } else {
                    this.innerMap[y][x] = Main.unknownCell;
                }
            }
        }
    }
}
