package test;

public class Main {
    public static final int mapW = 60;
    public static final int mapH = 60;
    public static final int renderRatio = 5;

    public static final int spaceshipX = mapW / 2;
    public static final int spaceshipY = mapH / 2;
    public static final int botsNumber = 10;
    public static final double obstacleRate = 0.15;
    public static final double stoneRate = 0.005;
    public static final int stonesMin = 40;
    public static final int stonesMax = 90;
    public static final int nothingCell = 0;
    public static final int unknownCell = -1;
    public static final int obstacleCell = -2;
    public static final int spaceshipCell = -3;

    public static final int visualisationsStep = 0;
    public static final int deletionStep = 50;
    public static final boolean visualiseWorldMap = true;
    public static final boolean visualiseSSMap = true;
    public static final boolean visualiseBotMap = true;

    public static final String spaceshipName = "Spaceship";
    public static final String botsPrefix = "bot_";
    public static final boolean communicationActivated = true;
    public static final boolean localGoalActivated = true;
    public static final long worldMapSeed = 1;

    public static void main(String[] args) {
        World grid = new World();
        grid.start();
        Renderer renderer = new Renderer(grid);
        renderer.start();
    }
}
