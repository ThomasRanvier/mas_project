package test.entities;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jdk.jshell.execution.Util;
import test.Main;
import test.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Bot extends Agent {
    protected int[][] innerMap = new int[Main.mapHeight][Main.mapWidth];
    protected int x = Main.spaceshipX;
    protected int y = Main.spaceshipY;
    protected int lastDx = 0;//Used to make the roaming smarter
    protected int lastDy = 0;
    protected int visualisationStep = 0;
    protected boolean holdsStone = false;

    @Override
    protected void setup(){
        System.out.println("Hi, I'm a little bot, " + this.getLocalName());
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
        //Find a way to inform World that the bot got a stone so that the worldMap can be updated

        while (true) {
            if (Main.visualiseBotMap && this.getLocalName().equals("bot_1")) {
                this.visualisationStep++;
                if (visualisationStep >= Main.visualisationsSteps) {
                    this.visualisationStep = 0;
                    this.writeMap();
                }
            }
            this.updateInnerMap();

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
                    this.goTo(closestStoneX, closestStoneY);
                } else {
                    //No stone detected
                    this.roamAround();
                }
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

    private void goTo(int x, int y) {
        //Path finding to the coords
        //Stays in that function until the bot actually reaches its destination
        //Check at each step if another bot is near
        //If so, merge the map with it
        //Store the name of the bot so we know that we do not merge maps with it before a little while
    }

    private void updateInnerMap() {
        //Update the map by registering the visible cells
        String cells;
        cells = Main.getCellsAround(this.x, this.y);
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
