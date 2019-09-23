package test;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BotMapVisualiser {
    private static String curLine = "";
    private static String prevLine = "";
    public static void main(String[] args) {
        JFrame frame = new JFrame("Bot map visualisation");
        Canvas canvas = new BotMapRenderer();
        canvas.setSize(Main.rendererWidth, Main.rendererHeight);
        frame.setVisible(true);
        try(BufferedReader br = new BufferedReader(new FileReader(Main.botMapFile))) {
            for(String line; (line = br.readLine()) != null; ) {
                prevLine = curLine;
                curLine = line;
                frame.add(canvas);
                frame.pack();
                TimeUnit.SECONDS.sleep(1);
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

    public static class BotMapRenderer extends Canvas {
        public void paint(Graphics g) {
            if (curLine.length() > 0) {
                String[] curLines = curLine.split(";");
                String[] prevLines = null;
                if (prevLine.length() > 0) {
                    prevLines = prevLine.split(";");
                }
                for (int y = 0; y < Main.mapHeight; y++) {
                    String[] curCells = curLines[y].split(",");
                    String[] prevCells = null;
                    if (prevLines != null) {
                        prevCells = prevLines[y].split(",");
                    }
                    for (int x = 0; x < Main.mapWidth; x++) {
                        int newX = x * Main.rendererStep;
                        int newY = y * Main.rendererStep;
                        int curCell = Integer.parseInt(curCells[x]);
                        int prevCell = Main.unknownCell;
                        if (prevCells != null) {
                            prevCell = Integer.parseInt(prevCells[x]);
                        }
//                        if (!(prevCells != null && prevCell == curCell)) {
                            if (curCell == Main.obstacleCell) {
                                g.setColor(Color.black);
                                g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                            } else if (curCell == Main.spaceshipCell) {
                                g.setColor(Color.blue);
                                g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                            } else if (curCell == Main.nothingCell) {
                                g.setColor(Color.white);
                                g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                            } else if (curCell == Main.unknownCell) {
                                g.setColor(Color.gray);
                                g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                            } else {
                                g.setColor(Color.green);
                                g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                            }
//                        }
                    }
                }
            }
        }
    }
}
