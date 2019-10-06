package test;

public class Main {
    public static final int mapW = 50;
    public static final int mapH = 50;
    public static final int renderRatio = 8;

    public static final int spaceshipX = mapW / 2;
    public static final int spaceshipY = mapH / 2;
    public static final int botsNumber = 4;
    public static final double obstacleRate = 0.15;
    public static final double stoneRate = 0.008;
    public static final int stonesPerPile = 50;
    public static final int nothingCell = 0;
    public static final int unknownCell = -1;
    public static final int obstacleCell = -2;
    public static final int spaceshipCell = -3;

    public static final int visualisationsStep = 0;
    public static final int deletionStep = 50;
    public static final int initialStep = 500;
    public static final boolean visualiseWorldMap = true;
    public static final boolean visualiseSSMap = true;
    public static final boolean visualiseBotMap = true;

    public static final String spaceshipName = "Spaceship";
    public static final String botsPrefix = "bot_";
    public static boolean communicationActivated;
    public static boolean localGoalActivated;
    public static boolean interBotCommunication;
    public static final long worldMapSeed = 0;//0 : seed not set

    public static void main(String[] args) {
        if (args.length == 3) {
            communicationActivated = args[0].equals("1");
            localGoalActivated = args[1].equals("1");
            interBotCommunication = args[2].equals("1");
            World grid = new World();
            grid.start();
            Renderer renderer = new Renderer(grid);
            renderer.start();
        } else {
            System.err.println("Veuillez renseigner les trois paramètres nécessaires");
            System.err.println("1 - Activation des communications");
            System.err.println("2 - Activation de l'exploration par objectifs locaux");
            System.err.println("3 - Activation des communications lors de la découverte d'un nouveau gisement");
        }
    }
}
