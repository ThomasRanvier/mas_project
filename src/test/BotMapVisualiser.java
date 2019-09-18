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
    public static void main(String[] args) {
        JFrame frame = new JFrame("Bot map visualisation");
        Canvas canvas = new BotMapRenderer();
        canvas.setSize(Main.rendererWidth, Main.rendererHeight);
        frame.setVisible(true);
        try(BufferedReader br = new BufferedReader(new FileReader(Main.botMapFile))) {
            for(String line; (line = br.readLine()) != null; ) {
                curLine = line;
                frame.add(canvas);
                frame.pack();
                TimeUnit.SECONDS.sleep(3);
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
            int y = 0;
            if (curLine.length() > 0) {
                for (String line : curLine.split(";")) {
                    if (line.length() > 0) {
                        int x = 0;
                        for (String cell : line.split(",")) {
                            if (cell.length() > 0) {
                                int newX = x * Main.rendererStep;
                                int newY = y * Main.rendererStep;
                                if (Integer.parseInt(cell) == Main.obstacleCell) {
                                    g.setColor(Color.black);
                                    g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                                } else if (Integer.parseInt(cell) == Main.spaceshipCell) {
                                    g.setColor(Color.blue);
                                    g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                                } else if (Integer.parseInt(cell) == Main.nothingCell) {
                                    g.setColor(Color.white);
                                    g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                                } else if (Integer.parseInt(cell) == Main.unknownCell) {
                                    g.setColor(Color.gray);
                                    g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                                } else {
                                    g.setColor(Color.green);
                                    g.fillRect(newX, newY, newX + Main.rendererStep, newY + Main.rendererStep);
                                }
                                x++;
                            }
                        }
                    }
                    y++;
                }
            }
        }
    }
}
