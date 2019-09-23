package test;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BotMapVisualiser {
    private static String line = "";
    private static String prevLine = "";

    public static Image drawMap(int w, int h) {
            BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics g = bufferedImage.getGraphics();

            if (line.length() > 0) {
                String[] curLines = line.split(";");
                String[] prevLines = null;
                if (prevLine.length() > 0) {
                    prevLines = prevLine.split(";");
                }
                for (int y = 0; y < Main.mapH; y++) {
                    String[] curCells = curLines[y].split(",");
                    String[] prevCells = null;
                    if (prevLines != null) {
                        prevCells = prevLines[y].split(",");
                    }
                    for (int x = 0; x < Main.mapW; x++) {
                        int curCell = Integer.parseInt(curCells[x]);
                        int prevCell = Main.unknownCell;
                        if (prevCells != null) {
                            prevCell = Integer.parseInt(prevCells[x]);
                        }

                        if (curCell == Main.obstacleCell) {
                            g.setColor(Color.black);
                        } else if (curCell == Main.spaceshipCell) {
                            g.setColor(Color.blue);
                        } else if (curCell == Main.nothingCell) {
                            g.setColor(Color.white);
                        } else if (curCell == Main.unknownCell) {
                            g.setColor(Color.gray);
                        } else {
                            g.setColor(Color.green);
                        }
                        g.drawLine(x,y,x,y);
                    }
                }
            }

            return bufferedImage;
        }
        public static Image imageUpscale (Image img){
            BufferedImage img2 = new BufferedImage(Main.mapW * Main.renderRatio, Main.mapH * Main.renderRatio, BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(Main.renderRatio, Main.renderRatio);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            img2 = scaleOp.filter((BufferedImage)img, img2);
            return img2;
        }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bot map visualisation");

        JLabel mapLabel = new JLabel();
        mapLabel.setSize(Main.mapW * Main.renderRatio, Main.mapH * Main.renderRatio);
        frame.setVisible(true);
        mapLabel.setIcon(new ImageIcon(imageUpscale(drawMap(Main.mapW,Main.mapH))));
        frame.add(mapLabel);

        try (BufferedReader br = new BufferedReader(new FileReader(Main.botMapFile))) {
            prevLine = line;
            line = br.readLine();

            while (line != null) {
                mapLabel.setIcon(new ImageIcon(imageUpscale(drawMap(Main.mapW,Main.mapH))));
                frame.repaint();
                frame.pack();
                TimeUnit.MILLISECONDS.sleep(500);
                prevLine = line;
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("End");
    }
}
