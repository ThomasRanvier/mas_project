package test;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import test.entities.Bot;
import test.entities.Spaceship;

import java.util.Random;

public class World {
    public int[][] map = new int[Main.mapHeight][Main.mapWidth];

    public World(){
        this.initialiseMap();
        ContainerController containerController = this.initJade();
        this.initSpaceship(containerController);
        this.initBots(containerController);
    }

    protected void initSpaceship(ContainerController containerController) {
        AgentController spaceshipController;
        try {
            spaceshipController = containerController.createNewAgent(Main.spaceshipName, Spaceship.class.getName(), null);
            spaceshipController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    protected void initBots(ContainerController containerController) {
        for(int i = 1; i <= Main.botsNumber; i++){
            AgentController botsController;
            try {
                botsController = containerController.createNewAgent(Main.botsPrefix + i, Bot.class.getName(), null);
                botsController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }

    protected ContainerController initJade() {
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.GUI, "true");
        ContainerController containerController = runtime.createMainContainer(profile);
        return containerController;
    }

    protected void initialiseMap() {
        //Initialises the map of the world, here we randomly set the location of the stones

        //-1 = inconnu (réservé à la représentation interne des agents)
        //-2 = peut marcher
        //-3 = obstacle
        //-4 = spaceship
        //(int) >= 0 = nombre de pierres sur cette case

        Random randomiser = new Random();
        for (int y = 0; y < Main.mapHeight; y++) {
            for (int x = 0; x < Main.mapWidth; x++) {
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
}
