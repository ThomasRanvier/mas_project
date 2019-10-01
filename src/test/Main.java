package test;

import javax.swing.JFrame;
import java.awt.*;
import java.io.*;

public class Main {
    public static final int mapW = 10;
    public static final int mapH = 10;
    public static final int renderRatio = 6;

    public static final int spaceshipX = mapW / 2;
    public static final int spaceshipY = mapH / 2;
    public static final int botsNumber = 10;
    public static final double obstacleRate = 0.15;
    public static final double stoneRate = 0.1;
    public static final int stonesMin = 10;
    public static final int stonesMax = 50;
    public static final int nothingCell = 0;
    public static final int unknownCell = -1;
    public static final int obstacleCell = -2;
    public static final int spaceshipCell = -3;

    public static final int visualisationsStep = 20;
    public static final boolean visualiseWorldMap = true;
    public static final boolean visualiseSSMap = true;
    public static final boolean visualiseBotMap = true;



    public static final String botMapFile = "src/test/botMap.txt";
    public static final String spaceshipName = "Spaceship";
    public static final String botsPrefix = "bot_";
    public static final boolean communicationActivated = true;

    public static void main(String[] args) {
        World grid = new World();
        Renderer renderer = new Renderer(grid);
        renderer.start();
    }
}
