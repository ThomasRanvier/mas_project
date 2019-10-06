package test.entities;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import test.Main;
import test.Utils;
import test.World;

import java.util.concurrent.TimeUnit;

/**
 * The spaceship class
 */
public class Spaceship extends Agent {
    private int stockedStones;
    private int stonesCount;
    private int[][] innerMap = new int[Main.mapW][Main.mapH];
    private World world;
    private long totalBotsMoves;

    @Override
    protected void setup(){
        Object[] args = this.getArguments();
        if (args != null && args.length > 0) {
            this.world = (World) args[0];
            this.stonesCount = (Integer) args[1];
        } else {
            System.err.println("Impossible to create spaceship if world is not set");
            this.doDelete();
        }
        System.out.println("Hello, I'm the spaceship, " + this.getLocalName());
        this.totalBotsMoves = 0;
        this.world.registerSpaceship(this);
        this.stockedStones = 0;
        this.initialiseInnerMap();
        try {
            TimeUnit.MILLISECONDS.sleep(Main.initialStep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.live();
        this.doDelete();
    }

    /**
     * The life cycle of the spaceship, its goal is to receive the maps from the bots and to combine them
     */
    private void live() {
        while (true) {
            ACLMessage msg = receive();
            if (msg != null) {
                String[] infos = msg.getContent().split(":");
                if (infos[1].equals("map")) {
                    if (Main.interBotCommunication) {
                        this.innerMap = Utils.stringToMap(infos[2]);
                        for (Bot b : this.world.getBots()) {
                            if (!b.getLocalName().equals(infos[0])) {
                                send(Utils.shareMap(this.getLocalName(), b.getLocalName(), infos[2]));
                            }
                        }
                    } else {
                        this.innerMap = Utils.mergeMaps(this.innerMap, infos[2]);
                        send(Utils.shareMap(this.getLocalName(), infos[0], Utils.mapToString(this.innerMap)));
                    }
                } else if (infos[1].equals("release")) {
                    this.stockedStones++;
                    this.totalBotsMoves += Integer.parseInt(infos[2]);
                    if (this.stockedStones >= this.stonesCount) {
                        System.out.println("Total bot moves : " + this.totalBotsMoves);
                        this.world.killJadeFlag = true;
                        //return;
                    }
                } else {
                    System.err.println("Weird msg : " + msg.getContent());
                }
            }
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("Taking down " + this.getLocalName());
        super.takeDown();
    }

    /**
     * Initialises the inner map
     */
    private void initialiseInnerMap() {
        for (int x = 0; x < Main.mapW; x++) {
            for (int y = 0; y < Main.mapH; y++) {
                if (x == Main.spaceshipX && y == Main.spaceshipY) {
                    this.innerMap[x][y] = Main.spaceshipCell;
                } else {
                    this.innerMap[x][y] = Main.unknownCell;
                }
            }
        }
    }

    public int[][] getInnerMap(){
        return this.innerMap;
    }

}
