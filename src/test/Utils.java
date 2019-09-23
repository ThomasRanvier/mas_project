package test;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Utils {
    public static boolean isInBoundaries(int newX, int newY) {
        return newX >= 0 && newX < Main.mapW && newY >= 0 && newY < Main.mapH;
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
        for (int y = 0; y < Main.mapH; y++) {
            for (int x = 0; x < Main.mapW; x++) {
                line += map[y][x] + ",";
            }
            line += ";";
        }
        return line;
    }

    public static int[][] stringToMap(String stringMap) {
        int[][] map = new int[Main.mapH][Main.mapW];
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

    public static Node nodeWithLowerCost(LinkedList<Node> openList, Map<Node, Double> fScore) {
        Node lower = openList.get(0);
        for (Node node : openList) {
            if (!fScore.containsKey(node)) {
                fScore.put(node, Double.POSITIVE_INFINITY);
            }
            if (fScore.get(node) < fScore.get(lower)) {
                lower = node;
            }
        }
        return lower;
    }

    public static List<Node> getNeighbours(Node node) {
        List<Node> neighbour = new LinkedList<Node>();
        for (int newX = node.x - 1; newX <= node.x + 1; newX++) {
            for (int newY = node.y - 1; newY <= node.y + 1; newY++) {
                if (Utils.isInBoundaries(newX, newY) && !(newX == node.x && newY == node.y)) {
                    neighbour.add(new Node(newX, newY));
                }
            }
        }
        return neighbour;
    }

    public static LinkedList<Node> reconstructPath(Map<Node, Node> cameFrom, Node current) {
        LinkedList<Node> path = new LinkedList<Node>();
        path.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(current);
        }
        return reverseLinkedList(path);
    }

    public static LinkedList<Node> reverseLinkedList(LinkedList<Node> llist) {
        LinkedList<Node> revLinkedList = new LinkedList<Node>();
        for (int i = llist.size() - 1; i >= 0; i--) {
            revLinkedList.add(llist.get(i));
        }
        return revLinkedList;
    }
}
