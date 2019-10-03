package test;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Collection of util functions
 */
public class Utils {
    /**
     * Check if a cell is in the boundaries of the world map
     * @param x coordinate
     * @param y coordinate
     * @return The result
     */
    public static boolean isInBoundaries(int x, int y) {
        return x >= 0 && x < Main.mapW && y >= 0 && y < Main.mapH;
    }

    /**
     * Used to share a string map to a receiver
     * @param senderName The name of the sender of the message
     * @param receiverName The name of the receiver
     * @param map The map in string format
     * @return The message to send
     */
    public static ACLMessage shareMap(String senderName, String receiverName, String map) {
        //Sends the inner map of the bot to the receiver
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiverName,AID.ISLOCALNAME));
        msg.setLanguage("English");
        msg.setContent(senderName + ":map:" + map);
        return msg;
    }

    /**
     * Converts a map in the int[][] format to the string format
     * @param map The map to convert
     * @return The converted map
     */
    public static String mapToString(int[][] map) {
        String line = "";
        for (int x = 0; x < Main.mapW; x++) {
            for (int y = 0; y < Main.mapH; y++) {
                line += map[x][y] + ",";
            }
            line += ";";
        }
        return line;
    }

    /**
     * Converts a map in the string format to the int[][] format
     * @param stringMap The map to convert
     * @return The converted map
     */
    public static int[][] stringToMap(String stringMap) {
        int[][] map = new int[Main.mapW][Main.mapH];
        int x = 0;
        if (stringMap.length() > 0) {
            for (String line : stringMap.split(";")) {
                if (line.length() > 0) {
                    int y = 0;
                    for (String cell : line.split(",")) {
                        if (cell.length() > 0) {
                            map[x][y] = Integer.parseInt(cell);
                            y++;
                        }
                    }
                }
                x++;
            }
        }
        return map;
    }

    /**
     * Returns the node with the lowest cost on the given list
     * @param openList The list of nodes
     * @param fScore The list of the fscores of the nodes
     * @return The lowest cost node
     */
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

    /**
     * Gives the direct neighbours of the given cell
     * @param node The cell
     * @return The neighbours as a Node list
     */
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

    /**
     * Reconstruct the path of the a* algorithm
     */
    public static LinkedList<Node> reconstructPath(Map<Node, Node> cameFrom, Node current) {
        LinkedList<Node> path = new LinkedList<Node>();
        path.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(current);
        }
        LinkedList<Node> reversed = reverseLinkedList(path);
        reversed.removeFirst();
        return reversed;
    }

    /**
     * Reverse a linked list
     */
    public static LinkedList<Node> reverseLinkedList(LinkedList<Node> llist) {
        LinkedList<Node> revLinkedList = new LinkedList<Node>();
        for (int i = llist.size() - 1; i >= 0; i--) {
            revLinkedList.add(llist.get(i));
        }
        return revLinkedList;
    }

    /**
     * Returns the distance between two cells
     */
    public static double calculateDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    /**
     * Combine two maps together
     * @param initialMap The initial map
     * @param mapToMerge The map to merge with the other
     * @return The merged maps
     */
    public static int[][] mergeMaps(int[][] initialMap, String mapToMerge) {
        int[][] botMap = Utils.stringToMap(mapToMerge);
        for (int x = 0; x < Main.mapW; x++) {
            for (int y = 0; y < Main.mapH; y++) {
                int cell1 = initialMap[x][y];
                int cell2 = botMap[x][y];
                if (cell1 > 0 && cell2 != Main.unknownCell && cell2 < cell1) {
                    initialMap[x][y] = cell2;
                } else if (cell1 == Main.unknownCell && cell2 != Main.unknownCell) {
                    initialMap[x][y] = cell2;
                }
            }
        }
        return initialMap;
    }
}
