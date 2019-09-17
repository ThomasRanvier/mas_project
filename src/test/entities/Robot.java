package test.entities;

import jade.core.Agent;
import test.Main;
import test.behaviours.BotLife;

public class Robot extends Agent {
    protected int[][] innerMap = new int[Main.mapHeight][Main.mapWidth];

    @Override
    protected void setup(){
        System.out.println("Hello, I'm a little bot, " + this.getLocalName());
        this.initialiseInnerMap();
        this.addBehaviour(new BotLife(innerMap));
    }

    @Override
    protected void takeDown() {
        System.out.println("Taking down " + this.getLocalName());
        super.takeDown();
    }

    protected void initialiseInnerMap() {
        //-1 = inconnu (réservé à la représentation interne des agents)
        //-2 = peut marcher
        //-3 = obstacle
        //-4 = spaceship
        //(int) >= 0 = nombre de pierres sur cette case
        int unknown = -1;
        int nothing = -2;
        int obstacle = -3;
        int spaceship = -4;

        for (int y = 0; y < Main.mapHeight; y++) {
            for (int x = 0; x < Main.mapWidth; x++) {
                if (x == Main.spaceshipX && y == Main.spaceshipY) {
                    this.innerMap[y][x] = spaceship;
                } else {
                    this.innerMap[y][x] = unknown;
                }
            }
        }
    }
}
