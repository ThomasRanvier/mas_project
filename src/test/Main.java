package test;

import javax.swing.JFrame;
import java.awt.*;
import java.io.*;

public class Main {
    public static final int mapWidth = 100;
    private static final double ratio = 1.0 / 2.0;
    public static final int mapHeight = (int)(ratio * ((double) mapWidth));
    public static final int spaceshipX = mapWidth / 2;
    public static final int spaceshipY = mapHeight / 2;
    public static final int botsNumber = 10;
    public static final double obstacleRate = 0.15;
    public static final double stoneRate = 0.005;
    public static final int stonesMin = 10;
    public static final int stonesMax = 50;
    public static final int unknownCell = -1;
    public static final int nothingCell = -2;
    public static final int obstacleCell = -3;
    public static final int spaceshipCell = -4;
    public static final int rendererWidth = 1500;
    public static final int rendererHeight = (int)(ratio * ((double) rendererWidth));
    public static final boolean visualiseBotMap = false;
    public static final int visualisationsSteps = 50;
    public static final boolean visualiseWorldMap = false;
    public static final int rendererStep = rendererWidth / mapWidth;
    public static final String botMapFile = "src/test/botMap.txt";
    public static final String spaceshipName = "TheBoss";
    public static final String botsPrefix = "bot_";
    public static final boolean communicationActivated = true;

    public static void main(String[] args) {
        if (visualiseBotMap) {
            PrintWriter pw;
            try {
                pw = new PrintWriter(botMapFile);
                pw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        World world = new World();
        if (visualiseWorldMap) {
            JFrame frame = new JFrame("World map visualisation");
            Canvas canvas = new World.Renderer();
            canvas.setSize(rendererWidth, rendererHeight);
            frame.add(canvas);
            frame.pack();
            frame.setVisible(true);
        }
    }
}
