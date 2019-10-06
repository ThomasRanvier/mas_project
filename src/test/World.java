package test;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import test.entities.Bot;
import test.entities.Spaceship;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The World class, that contains the world map
 */
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
        if (Main.communicationActivated) {
            System.out.println("Communications activées");
        } else {
            System.out.println("Communications désactivées");
        }
        if (Main.localGoalActivated) {
            System.out.println("Exploration par objectifs locaux activée");
        } else {
            System.out.println("Exploration par l'aléatoire activée");
        }
        if (Main.communicationActivated) {
            if (Main.interBotCommunication) {
                System.out.println("Communication lors de la découverte d'un nouveau gisement");
            } else {
                System.out.println("Communication lors du retour au vaisseau");
            }
        }
        System.out.println("Stone piles count : " + stoneCount / Main.stonesPerPile);
        this.initSpaceship(stoneCount);
        this.initBots();
    }

    public void run() {
        while (!killJadeFlag) {
            try {TimeUnit.MILLISECONDS.sleep(Main.deletionStep);}
            catch (InterruptedException e) {e.printStackTrace();}
        }
        this.killJade();
    }

    /**
     * Used by the spaceship when it spawns so that the world knows its reference
     * @param ss The spaceship reference
     */
    public void registerSpaceship(Spaceship ss) {
        this.ss = ss;
    }

    /**
     * Used by the bots when they spawn so that the world knows their references
     * @param bot The bot reference
     */
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

    /**
     * Initialises the spaceship
     * @param stonesCount The stones count on the map
     */
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

    /**
     * Initialises the bots
     */
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

    /**
     * Used by the bots when they want to take a stone, so that the world is aware of it
     * @param stone The stone to take
     * @return True if the bot got the stone, false if there is no stone left
     */
    public boolean[] takeStone(Node stone) {
        boolean valid = false;
        boolean discovery = false;
        lock.lock();
        try {
            if (map[stone.x][stone.y] > 0) {
                if (map[stone.x][stone.y] == Main.stonesPerPile) {discovery = true;}
                map[stone.x][stone.y]--;
                valid = true;
                return new boolean[]{valid, discovery};
            }
        } catch (Exception e) {
            System.err.println("Several threads trying to access method takeStone");
        } finally {
            lock.unlock();
        }
        return new boolean[]{valid, discovery};
    }

    /**
     * Gives the cells around in a string format, used by the bots to discover their environment
     * @param x X coordinate of the bot
     * @param y Y coordinate of the bot
     * @return The cells around in a string
     */
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

    /**
     * Initialising Jade
     */
    private void initJade() {
        this.runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.GUI, "false");
        this.cc = this.runtime.createMainContainer(profile);
    }

    /**
     * Initialises the world map
     * @return The stones count on the map that will be used by the spaceship to know when the process is over
     */
    private int initialiseMap() {
        int stonesCount = 0;
        Random randomiser = null;
        if (Main.worldMapSeed == 0) {
            randomiser = new Random();
        } else {
            randomiser = new Random(Main.worldMapSeed);
        }
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
                            this.map[x][y] = Main.stonesPerPile;
                            stonesCount += Main.stonesPerPile;
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
    }

    public HashSet<Bot> getBots() {
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
