package test;

import javax.swing.JFrame;
import java.awt.*;
import java.io.*;

public class Main {
    public static int mapWidth = 150;
    private static double ratio = 1.0 / 2.0;
    public static int mapHeight = (int)(ratio * ((double) mapWidth));
    public static int spaceshipX = mapWidth / 2;
    public static int spaceshipY = mapHeight / 2;
    public static int botsNumber = 3;
    public static double obstacleRate = 0.15;
    public static double stoneRate = 0.005;
    public static int stonesMin = 10;
    public static int stonesMax = 50;
    public static int unknownCell = -1;
    public static int nothingCell = -2;
    public static int obstacleCell = -3;
    public static int spaceshipCell = -4;
    private static World world;
    public static int rendererWidth = 1500;
    public static int rendererHeight = (int)(ratio * ((double) rendererWidth));
    public static boolean visualiseBotMap = false;
    public static int visualisationsSteps = 10000;
    public static boolean visualiseWorldMap = true;
    public static int rendererStep = rendererWidth / mapWidth;
    public static String botMapFile = "src/test/botMap.txt";
    public static String spaceshipName = "TheBoss";
    public static String botsPrefix = "bot_";

    public static void main(String[] args) {
        if (visualiseBotMap) {
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(botMapFile);
                pw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        world = new World();
        if (visualiseWorldMap) {
            JFrame frame = new JFrame("World map visualisation");
            Canvas canvas = new Renderer(world);
            canvas.setSize(rendererWidth, rendererHeight);
            frame.add(canvas);
            frame.pack();
            frame.setVisible(true);
        }
    }

    public static String getCellsAround(int x, int y) {
        String cells = "";
        for (int newX = x - 1; newX <= x + 1; newX++) {
            for (int newY = y - 1; newY <= y + 1; newY++) {
                if (Utils.isInBoundaries(newX, newY)) {
                    cells += newY + "," + newX + "," + world.map[newY][newX] + ";";
                }
            }
        }
        return cells;
    }
}
