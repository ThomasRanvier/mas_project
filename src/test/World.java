package test;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import test.entities.Bot;
import test.entities.Spaceship;

import java.awt.*;
import java.util.Random;

public class World {
    private static int[][] map = new int[Main.mapH][Main.mapW];

    public World(){
        this.initialiseMap();
        ContainerController containerController = this.initJade();
        this.initSpaceship(containerController);
        this.initBots(containerController);
    }

    private void initSpaceship(ContainerController containerController) {
        AgentController spaceshipController;
        try {
            Object[] ssArgs = {this};
            spaceshipController = containerController.createNewAgent(Main.spaceshipName, Spaceship.class.getName(), ssArgs);
            spaceshipController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private void initBots(ContainerController containerController) {
        for(int i = 1; i <= Main.botsNumber; i++){
            AgentController botsController;
            try {
                Object[] botArgs = {this};
                botsController = containerController.createNewAgent(Main.botsPrefix + i, Bot.class.getName(), botArgs);
                botsController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean takeStone(int stoneX, int stoneY, String botName) {
        if (map[stoneY][stoneX] > 0) {
            map[stoneY][stoneX]--;
            return true;
        }
        return false;
    }

    public String getCellsAround(int x, int y) {
        String cells = "";
        for (int newX = x - 1; newX <= x + 1; newX++) {
            for (int newY = y - 1; newY <= y + 1; newY++) {
                if (Utils.isInBoundaries(newX, newY)) {
                    cells += newY + "," + newX + "," + this.map[newY][newX] + ";";
                }
            }
        }
        return cells;
    }

    private ContainerController initJade() {
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.GUI, "true");
        ContainerController containerController = runtime.createMainContainer(profile);
        return containerController;
    }

    private void initialiseMap() {
        //Initialises the map of the world, here we randomly set the location of the stones

        //-1 = inconnu (réservé à la représentation interne des agents)
        //-2 = peut marcher
        //-3 = obstacle
        //-4 = spaceship
        //(int) >= 0 = nombre de pierres sur cette case

        Random randomiser = new Random();
        for (int y = 0; y < Main.mapH; y++) {
            for (int x = 0; x < Main.mapW; x++) {
                if (x == Main.spaceshipX && y == Main.spaceshipY) {
                    this.map[y][x] = Main.spaceshipCell;
                } else {
                    int rand = randomiser.nextInt(1000) + 1;
                    if (rand <= (int)(Main.obstacleRate * 1000.0)) {
                        this.map[y][x] = Main.obstacleCell;
                    } else {
                        rand = randomiser.nextInt(1000) + 1;
                        if (rand <= (int)(Main.stoneRate * 1000.0)) {
                            rand = randomiser.nextInt(Main.stonesMax - Main.stonesMin) + Main.stonesMin;
                            this.map[y][x] = rand;
                        } else {
                            this.map[y][x] = Main.nothingCell;
                        }
                    }
                }
            }
        }
    }

    public static class Renderer extends Canvas {
        public void paint(Graphics g) {
            for (int y = 0; y < Main.mapH; y++) {
                for (int x = 0; x < Main.mapW; x++) {
                    int newX = x * Main.renderRatio;
                    int newY = y * Main.renderRatio;
                    if (map[y][x] == Main.obstacleCell) {
                        g.setColor(Color.black);
                        g.fillRect(newX, newY, newX + Main.renderRatio, newY + Main.renderRatio);
                    } else if (map[y][x] == Main.spaceshipCell) {
                        g.setColor(Color.blue);
                        g.fillRect(newX, newY, newX + Main.renderRatio, newY + Main.renderRatio);
                    } else if (map[y][x] == Main.nothingCell) {
                        g.setColor(Color.white);
                        g.fillRect(newX, newY, newX + Main.renderRatio, newY + Main.renderRatio);
                    } else {
                        g.setColor(Color.green);
                        g.fillRect(newX, newY, newX + Main.renderRatio, newY + Main.renderRatio);
                    }
                }
            }
        }
    }
}
