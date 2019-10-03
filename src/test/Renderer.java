package test;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import test.entities.Bot;
import test.entities.Spaceship;

/**
 * The Renderer class, used to visualise the maps
 */
public class Renderer extends Thread {
    private World world;
    private int[][] worldmap;
    private Spaceship ss;
    private int[][] ssmap;
    private HashSet<Bot> agents;
    private int[][] botmap;

    public Renderer(World world) {
        this.world = world;
        this.worldmap = this.world.getMap();
        this.ss = this.world.getSpaceship();
        this.ssmap = ss.getInnerMap();
        this.agents = this.world.getBots();
        this.botmap = this.agents.iterator().next().getInnerMap();
    }

    public void run(){
        JFrame frame = new JFrame("World Map");
        frame.setLayout(new GridLayout(1, (Main.visualiseWorldMap?1:0) + (Main.visualiseSSMap?1:0) + (Main.visualiseBotMap?1:0) ));

        JLabel worldMapLabel = new JLabel();
        if(Main.visualiseWorldMap) {
        worldMapLabel.setSize(Main.mapW * Main.renderRatio, Main.mapH * Main.renderRatio);
        worldMapLabel.setIcon(new ImageIcon(imageUpscale(drawMap(Main.mapW, Main.mapH, this.worldmap))));
        frame.add(worldMapLabel);
        }

        JLabel ssMapLabel = new JLabel();
        if(Main.visualiseSSMap) {
            ssMapLabel.setSize(Main.mapW * Main.renderRatio, Main.mapH * Main.renderRatio);
            ssMapLabel.setIcon(new ImageIcon(imageUpscale(drawMap(Main.mapW, Main.mapH, this.ssmap))));
            frame.add(ssMapLabel);
        }

        JLabel botMapLabel = new JLabel();
        if(Main.visualiseBotMap) {
            botMapLabel.setSize(Main.mapW * Main.renderRatio, Main.mapH * Main.renderRatio);
            botMapLabel.setIcon(new ImageIcon(imageUpscale(drawMap(Main.mapW, Main.mapH, this.botmap))));
            frame.add(botMapLabel);
        }

        frame.setVisible(true);

        while (this.world.isAlive()) {
            if(Main.visualiseWorldMap){this.worldmap = this.world.getMap();}
            if(Main.visualiseSSMap){this.ssmap = this.ss.getInnerMap();}
            if(Main.visualiseBotMap){this.botmap = this.agents.iterator().next().getInnerMap();}

            worldMapLabel.setIcon(new ImageIcon(imageUpscale(drawMap(Main.mapW, Main.mapH, this.worldmap))));
            ssMapLabel.setIcon(new ImageIcon(imageUpscale(drawMap(Main.mapW, Main.mapH, this.ssmap))));
            botMapLabel.setIcon(new ImageIcon(imageUpscale(drawMap(Main.mapW, Main.mapH, this.botmap))));

            frame.repaint();
            frame.pack();
            try {
                TimeUnit.MILLISECONDS.sleep(Main.visualisationsStep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Image drawMap(int w, int h,  int[][] cur_map){
        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        for (int x = 0; x < Main.mapW; x++) {
            for (int y = 0; y < Main.mapH; y++) {
                if (cur_map[x][y] == Main.obstacleCell) {
                    g.setColor(Color.black);
                } else if (cur_map[x][y] == Main.spaceshipCell) {
                    g.setColor(Color.blue);
                } else if (cur_map[x][y] == Main.nothingCell || cur_map[x][y] == 0) {
                    g.setColor(Color.white);
                } else if (cur_map[x][y] == Main.unknownCell) {
                    g.setColor(Color.gray);
                } else {
                    g.setColor(Color.red);
                }
                g.drawLine(x, y, x, y);
            }
        }
        g.setColor(Color.green);
        for(Bot bot_i : agents) {
            int cur_x = bot_i.getBotX();
            int cur_y = bot_i.getBotY();
            g.drawLine(cur_x, cur_y, cur_x, cur_y);
        }
        return bufferedImage;
    }

    public static Image imageUpscale(Image img) {
        BufferedImage img2 = new BufferedImage(Main.mapW * Main.renderRatio, Main.mapH * Main.renderRatio, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(Main.renderRatio, Main.renderRatio);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        img2 = scaleOp.filter((BufferedImage) img, img2);
        return img2;
    }
}