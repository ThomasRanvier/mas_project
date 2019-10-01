package test;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import test.entities.Bot;
import test.entities.Spaceship;

import java.awt.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class World extends Thread {
    private static int[][] map = new int[Main.mapW][Main.mapH];
    private Set<Bot> bots = new HashSet<Bot>();
    private Spaceship ss;
    private final Lock lock = new ReentrantLock(true);
    private Runtime runtime;
    private ContainerController cc;
    public boolean killJadeFlag = false;

    public World() {
        int stoneCount = this.initialiseMap();
        this.initJade();
        this.initSpaceship(stoneCount);
        this.initBots();
    }

    public void run() {
        while (!killJadeFlag) {
            try {TimeUnit.MILLISECONDS.sleep(Main.visualisationsStep);}
            catch (InterruptedException e) {e.printStackTrace();}
        }
        this.killJade();
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

    private void initSpaceship(int stonesCount) {
        AgentController spaceshipController;
        try {
            Object[] ssArgs = {this, stonesCount};
            spaceshipController = this.cc.createNewAgent(Main.spaceshipName, Spaceship.class.getName(), ssArgs);
            spaceshipController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private void initBots() {
        for(int i = 1; i <= Main.botsNumber; i++){
            AgentController botsController;
            try {
                Object[] botArgs = {this};
                botsController = this.cc.createNewAgent(Main.botsPrefix + i, Bot.class.getName(), botArgs);
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
            if (map[stoneX][stoneY] > 0) {
                map[stoneX][stoneY]--;
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
                        cells += newX + "," + newY + "," + this.map[newX][newY] + ";";
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

    private void initJade() {
        this.runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.GUI, "true");
        this.cc = this.runtime.createMainContainer(profile);
    }

    private int initialiseMap() {
        int stonesCount = 0;
        Random randomiser = new Random(1);
        for (int x = 0; x < Main.mapW; x++) {
            for (int y = 0; y < Main.mapH; y++) {
                if (x == Main.spaceshipX && y == Main.spaceshipY) {
                    this.map[x][y] = Main.spaceshipCell;
                } else {
                    int rand = randomiser.nextInt(1000) + 1;
                    if (rand <= (int)(Main.obstacleRate * 1000.0)) {
                        this.map[x][y] = Main.obstacleCell;
                    } else {
                        rand = randomiser.nextInt(1000) + 1;
                        if (rand <= (int)(Main.stoneRate * 1000.0)) {
                            rand = randomiser.nextInt(Main.stonesMax - Main.stonesMin) + Main.stonesMin;
                            this.map[x][y] = rand;
                            stonesCount += rand;
                        } else {
                            this.map[x][y] = Main.nothingCell;
                        }
                    }
                }
            }
        }
        return stonesCount;
    }

    private void killJade() {
        System.out.println("The end");
        for (Bot bot : this.bots) {
            bot.deathFlag = true;
        }
        try {TimeUnit.MILLISECONDS.sleep(Main.visualisationsStep*2);}
        catch (InterruptedException e) {e.printStackTrace();}
        try {
            this.cc.kill();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        this.runtime.shutDown();
    }

    public HashSet<Bot> getAgents() {
        lock.lock();
        try {
            return (HashSet<Bot>) this.bots;
        } catch (Exception e) {
            System.err.println("Several threads trying to access method getAgent");
        } finally {
            lock.unlock();
        }
        return null;
    }

    public Spaceship getSpaceship() {
        lock.lock();
        try {
            return this.ss;
        } catch (Exception e) {
            System.err.println("Several threads trying to access method getAgent");
        } finally {
            lock.unlock();
        }
        return null;
    }

    public int[][] getMap(){
        lock.lock();
        try {
            return this.map;
        } catch (Exception e) {
            System.err.println("Several threads trying to access method getMap");
        } finally {
            lock.unlock();
        }
        return null;
    }



}
