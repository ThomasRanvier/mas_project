package test;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class Utils {
    public static boolean isInBoundaries(int newX, int newY) {
        return newX >= 0 && newX < Main.mapWidth && newY >= 0 && newY < Main.mapHeight;
    }

    public static ACLMessage shareMap(String senderName, String receiverName, String map) {
        //Sends the inner map of the bot to the receiver
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiverName,AID.ISLOCALNAME));
        msg.setLanguage("English");
        msg.setContent(senderName + ":map:" + map);
        return msg;
    }

    public static String mapToString(int[][] map) {
        String line = "";
        for (int y = 0; y < Main.mapHeight; y++) {
            for (int x = 0; x < Main.mapWidth; x++) {
                line += map[y][x] + ",";
            }
            line += ";";
        }
        return line;
    }

    public static int[][] stringToMap(String stringMap) {
        int[][] map = new int[Main.mapHeight][Main.mapWidth];
        int y = 0;
        if (stringMap.length() > 0) {
            for (String line : stringMap.split(";")) {
                if (line.length() > 0) {
                    int x = 0;
                    for (String cell : line.split(",")) {
                        if (cell.length() > 0) {
                            map[y][x] = Integer.parseInt(cell);
                            x++;
                        }
                    }
                }
                y++;
            }
        }
        return map;
    }
}
