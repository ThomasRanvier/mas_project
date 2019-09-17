package test;

import java.awt.*;

public class Renderer extends Canvas {
    private World world;
    public Renderer(World world) {
        this.world = world;
    }

    public void paint(Graphics g) {
        g.setColor(Color.gray);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        for (int y = 0; y < Main.mapHeight; y++) {
            for (int x = 0; x < Main.mapWidth; x++) {
                int newX = x * Main.rendererStep;
                int newY = y * Main.rendererStep;
                if (this.world.map[y][x] == Main.obstacleCell) {
                    g.setColor(Color.black);
                    g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                } else if (this.world.map[y][x] == Main.spaceshipCell) {
                    g.setColor(Color.blue);
                    g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                } else if (this.world.map[y][x] == Main.nothingCell) {
                    g.setColor(Color.white);
                    g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                } else {
                    g.setColor(Color.green);
                    g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                }
            }
        }
    }
}