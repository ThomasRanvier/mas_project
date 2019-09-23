package test;


import jdk.jshell.execution.Util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Tests {
    public static void main(String[] args) {
        testReconstructPath();
    }

    public static void testReconstructPath() {
        Map<Node, Node> cameFrom = new HashMap<Node, Node>();
        Node current = new Node(5, 5);
        cameFrom.put(new Node(2, 2), new Node(1, 1));
        cameFrom.put(new Node(5, 5), new Node(4, 4));
        cameFrom.put(new Node(4, 4), new Node(3, 3));
        cameFrom.put(new Node(3, 3), new Node(2, 2));
        LinkedList<Node> path = Utils.reconstructPath(cameFrom, current);
        for (Node node : path) {
            System.out.println(node);
        }
    }

    public static void testReverseLList() {
        LinkedList<Node> ordered = new LinkedList<Node>();
        ordered.add(new Node(1, 1));
        ordered.add(new Node(2, 2));
        ordered.add(new Node(3, 3));
        ordered.add(new Node(4, 4));
        ordered.add(new Node(5, 5));
        for (Node node : ordered) {
            System.out.println(node);
        }
        System.out.println("Reversed");
        LinkedList<Node> reversed = Utils.reverseLinkedList(ordered);
        for (Node node : reversed) {
            System.out.println(node);
        }
    }

    public static void testStringToMap() {
        String stringMap = "-1,-1,-2,-3,-4,-1,-1,5;-1,-1,-1,-2,-2,-2,-3,-3;5,5,9,9,-4,-4,-4,-4;";
        int[][] map = Utils.stringToMap(stringMap);
        for (int y = 0; y < Main.mapH; y++) {
            String line = "";
            for (int x = 0; x < Main.mapW; x++) {
                line += map[y][x] + " ";
            }
            System.out.println(line);
        }
        System.out.println(Utils.mapToString(map));
    }
}
