package test;

import javax.swing.JFrame;
import java.awt.*;
import java.io.*;

public class Main {
    public static final int mapW = 400;
    public static final int mapH = 300;
    public static final int renderRatio = 3;

    public static final int spaceshipX = mapW / 2;
    public static final int spaceshipY = mapH / 2;
    public static final int botsNumber = 1;
    public static final double obstacleRate = 0.15;
    public static final double stoneRate = 0.005;
    public static final int stonesMin = 10;
    public static final int stonesMax = 50;
    public static final int unknownCell = -1;
    public static final int nothingCell = -2;
    public static final int obstacleCell = -3;
    public static final int spaceshipCell = -4;
    public static final boolean visualiseBotMap = true;
    public static final int visualisationsSteps = 10;//10000;
    public static final boolean visualiseWorldMap = true;

    public static final String botMapFile = "src/test/botMap.txt";
    public static final String spaceshipName = "TheBoss";
    public static final String botsPrefix = "bot_";
    private static World world;

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
        world = new World();
        if (visualiseWorldMap) {
            JFrame frame = new JFrame("World map visualisation");
            Canvas canvas = new World.Renderer();
            canvas.setSize(mapW * renderRatio, mapH * renderRatio);
            frame.add(canvas);
            frame.pack();
            frame.setVisible(true);
        }
    }
}
