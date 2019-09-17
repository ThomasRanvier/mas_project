package test.entities;

import jade.core.Agent;
import test.Main;

import java.util.Random;

public class Bot extends Agent {
    protected int[][] innerMap = new int[Main.mapHeight][Main.mapWidth];
    protected int x = Main.spaceshipX;
    protected int y = Main.spaceshipY;
    protected int lastDx = 0;//Used to make the roaming smarter
    protected int lastDy = 0;

    @Override
    protected void setup(){
        System.out.println("Hi, I'm a little bot, " + this.getLocalName());
        this.initialiseInnerMap();
        //this.addBehaviour(new BotLife(innerMap));
        this.live();
    }

    @Override
    protected void takeDown() {
        System.out.println("Taking down " + this.getLocalName());
        super.takeDown();
    }

    protected void live() {
        //The life cycle of the robot

        while (true) {
            if (Main.visualiseBotMap && this.getLocalName().equals("bot_1")) {
                Main.visualiseMap(this.innerMap);
            }
            this.updateInnerMap();

            int[] closestStoneCoords = this.getClosestStone();
            int closestStoneX = closestStoneCoords[1];
            int closestStoneY = closestStoneCoords[0];

            if (closestStoneX >= 0) {
                //Stone detected
                System.out.println("Stone detected : " + closestStoneX + ", " + closestStoneY);
                this.goTo(closestStoneX, closestStoneY);
            } else {
                //No stone detected
                this.roamAround();
            }
        }
    }

    private void roamAround() {
        //Randomly roam around
        Random randomiser = new Random();
        if (randomiser.nextInt(3) == 0) {//1 chance out of 3 to not change its orientation, makes roaming more fluid
            int newX = this.x + lastDx;
            int newY = this.y + lastDy;
            if (Main.isInBoundaries(newX, newY)) {
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
            if (Main.isInBoundaries(newX, newY)) {
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

    protected void initialiseInnerMap() {
        //-1 = inconnu (réservé à la représentation interne des agents)
        //-2 = peut marcher
        //-3 = obstacle
        //-4 = spaceship
        //(int) >= 0 = nombre de pierres sur cette case

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
