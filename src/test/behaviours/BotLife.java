package test.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import test.Main;

public class BotLife extends CyclicBehaviour {
    protected int[][] innerMap;

    public BotLife(int[][] innerMap) {
        this.innerMap = innerMap;
    }

    @Override
    public void action() {
        //While no stone has been registered to the map : roam around and complete inner map
        //When encounters other bot : merge maps // A voir, peut potentiellement poser problème lorsque beacoup de robots se retrouvent en même temps prés du spaceship ou près des pierres, mieux vaut surement n'avoir qu'une map centralisée au spaceship
        //When reaches a stone : get one out of the stack
        //When holds a stone : go back to the spaceship (using a path finding algo on the inner map)
        //When reaches spaceship : get rid of the stone and merge maps

        //System.out.println("Roam around");
        for (int y = 0; y < Main.mapHeight; y++) {
            String line = "";
            for (int x = 0; x < Main.mapWidth; x++) {
                line += " " + Integer.toString(this.innerMap[y][x]) + " ";
            }
            System.out.println(line);
        }
    }
}
