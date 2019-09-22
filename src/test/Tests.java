package test;

public class Tests {
    public static void main(String[] args) {
        String stringMap = "-1,-1,-2,-3,-4,-1,-1,5;-1,-1,-1,-2,-2,-2,-3,-3;5,5,9,9,-4,-4,-4,-4;";
        int[][] map = Utils.stringToMap(stringMap);
        for (int y = 0; y < Main.mapHeight; y++) {
            String line = "";
            for (int x = 0; x < Main.mapWidth; x++) {
                line += map[y][x] + " ";
            }
            System.out.println(line);
        }
        System.out.println(Utils.mapToString(map));
    }
}
