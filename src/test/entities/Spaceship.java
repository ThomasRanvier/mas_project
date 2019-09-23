package test.entities;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import test.Main;
import test.Utils;
import test.World;

public class Spaceship extends Agent {
    private int stockedStones;
    private int[][] innerMap = new int[Main.mapH][Main.mapW];
    private World world;

    @Override
    protected void setup(){
        Object[] args = this.getArguments();
        if (args != null && args.length > 0) {
            this.world = (World) args[0];
        } else {
            System.err.println("Impossible to create spaceship if world is not set");
            this.doDelete();
        }
        System.out.println("Hello, I'm the boss, " + this.getLocalName());
        this.stockedStones = 0;
        this.initialiseInnerMap();
        this.live();
    }

    private void live() {
        while (true) {
            ACLMessage msg = receive();
            if (msg != null) {
                String[] infos = msg.getContent().split(":");
                if (infos[1].equals("map")) {
                    this.mergeMaps(infos[2]);
                    send(Utils.shareMap(this.getLocalName(), infos[0], Utils.mapToString(this.innerMap)));
                } else if (infos[1].equals("release")) {
                    this.stockedStones += Integer.parseInt(infos[2]);
                } else {
                    System.err.println("Weird msg : " + msg.getContent());
                }
            }
        }
    }

    private void mergeMaps(String botInnerMap) {
        int[][] botMap = Utils.stringToMap(botInnerMap);
        for (int y = 0; y < Main.mapH; y++) {
            for (int x = 0; x < Main.mapW; x++) {
                int ssCell = this.innerMap[y][x];
                int botCell = botMap[y][x];
                if (ssCell > 0 && botCell != Main.unknownCell && botCell < ssCell) {
                    this.innerMap[y][x] = botCell;
                } else if (ssCell == Main.unknownCell && botCell != Main.unknownCell) {
                    this.innerMap[y][x] = botCell;
                }
            }
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("Taking down " + this.getLocalName());
        super.takeDown();
    }

    private void initialiseInnerMap() {
        for (int y = 0; y < Main.mapH; y++) {
            for (int x = 0; x < Main.mapW; x++) {
                if (x == Main.spaceshipX && y == Main.spaceshipY) {
                    this.innerMap[y][x] = Main.spaceshipCell;
                } else {
                    this.innerMap[y][x] = Main.unknownCell;
                }
            }
        }
    }
}
