package test;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.Random;

public class World {
    protected int[][] map = new int[Main.mapHeight][Main.mapWidth];

    public World(){
        this.initialiseMap();
        this.visualiseMap();

        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.GUI, "true");
        ContainerController containerController = runtime.createMainContainer(profile);

        AgentController spaceshipController;
        try {
            spaceshipController = containerController.createNewAgent("TheBoss", "test.entities.Spaceship", null);
            spaceshipController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        for(int i = 1; i <= Main.botsNumber; i++){
            AgentController botsController;
            try {
                botsController = containerController.createNewAgent("bot_" + i, "test.entities.Robot", null);
                botsController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    protected void initialiseMap() {
        //Initialises the map of the world, here we randomly set the location of the stones

        //-1 = inconnu (réservé à la représentation interne des agents)
        //-2 = peut marcher
        //-3 = obstacle
        //-4 = spaceship
        //(int) >= 0 = nombre de pierres sur cette case
        int unknown = -1;
        int nothing = -2;
        int obstacle = -3;
        int spaceship = -4;

        Random randomiser = new Random();
        for (int y = 0; y < Main.mapHeight; y++) {
            for (int x = 0; x < Main.mapWidth; x++) {
                if (x == Main.spaceshipX && y == Main.spaceshipY) {
                    this.map[y][x] = spaceship;
                } else {
                    int rand = randomiser.nextInt(100) + 1;
                    if (rand <= (int)(Main.obstacleRate * 100)) {
                        this.map[y][x] = obstacle;
                    } else {
                        rand = randomiser.nextInt(100) + 1;
                        if (rand <= (int)(Main.stoneRate * 100)) {
                            rand = randomiser.nextInt(Main.stonesMax - Main.stonesMin) + Main.stonesMin;
                            this.map[y][x] = rand;
                        } else {
                            this.map[y][x] = nothing;
                        }
                    }
                }
            }
        }
    }

    protected void visualiseMap() {
        for (int y = 0; y < Main.mapHeight; y++) {
            String line = "";
            for (int x = 0; x < Main.mapWidth; x++) {
                line += " " + Integer.toString(this.map[y][x]) + " ";
            }
            System.out.println(line);
        }
    }
}
