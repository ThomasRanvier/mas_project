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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class World {
    private static int[][] map = new int[Main.mapHeight][Main.mapWidth];
    private Set<Bot> bots = new HashSet<Bot>();
    private Spaceship ss;
    private final Lock lock = new ReentrantLock(true);
    private Runtime runtime;

    public World(){
        int stoneCount = this.initialiseMap();
        ContainerController containerController = this.initJade();
        this.initSpaceship(containerController, stoneCount);
        this.initBots(containerController);
    }

    public void registerSpaceship(Spaceship ss) {
        this.ss = ss;
    }

    public void registerBots(Bot bot) {
        lock.lock();
        try {
            this.bots.add(bot);
        } catch (Exception e) {
            System.err.println("Several threads trying to access method registerBots");
        } finally {
            lock.unlock();
        }
    }

    private void initSpaceship(ContainerController containerController, int stonesCount) {
        AgentController spaceshipController;
        try {
            Object[] ssArgs = {this, stonesCount};
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
        boolean valid = false;
        lock.lock();
        try {
            if (map[stoneY][stoneX] > 0) {
                map[stoneY][stoneX]--;
                valid = true;
            }
        } catch (Exception e) {
            System.err.println("Several threads trying to access method takeStone");
        } finally {
            lock.unlock();
        }
        return valid;
    }

    public String getCellsAround(int x, int y) {
        String cells = "";
        lock.lock();
        try {
            for (int newX = x - 1; newX <= x + 1; newX++) {
                for (int newY = y - 1; newY <= y + 1; newY++) {
                    if (Utils.isInBoundaries(newX, newY)) {
                        cells += newY + "," + newX + "," + this.map[newY][newX] + ";";
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Several threads trying to access method getCellsAround");
        } finally {
            lock.unlock();
        }
        return cells;
    }

    private ContainerController initJade() {
        this.runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.GUI, "true");
        ContainerController containerController = runtime.createMainContainer(profile);
        return containerController;
    }

    private int initialiseMap() {
        int stonesCount = 0;
        Random randomiser = new Random(1);
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
                            stonesCount += rand;
                        } else {
                            this.map[y][x] = Main.nothingCell;
                        }
                    }
                }
            }
        }
        return stonesCount;
    }

    public void killJade() {
        System.out.println("The end");
        for (Bot bot : this.bots) {
            bot.doDelete();
        }
        this.ss.doDelete();
        this.runtime.shutDown();
    }

    public static class Renderer extends Canvas {
        public void paint(Graphics g) {
            for (int y = 0; y < Main.mapHeight; y++) {
                for (int x = 0; x < Main.mapWidth; x++) {
                    int newX = x * Main.rendererStep;
                    int newY = y * Main.rendererStep;
                    if (map[y][x] == Main.obstacleCell) {
                        g.setColor(Color.black);
                        g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                    } else if (map[y][x] == Main.spaceshipCell) {
                        g.setColor(Color.blue);
                        g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                    } else if (map[y][x] == Main.nothingCell) {
                        g.setColor(Color.white);
                        g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                    } else {
                        g.setColor(Color.green);
                        g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                    }
                }
            }
        }
    }
}
