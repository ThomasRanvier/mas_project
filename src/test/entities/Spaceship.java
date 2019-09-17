package test.entities;

import jade.core.Agent;

public class Spaceship extends Agent {
    @Override
    protected void setup(){
        System.out.println("Hello, I'm the boss, " + this.getLocalName());
    }

    @Override
    protected void takeDown() {
        System.out.println("Taking down " + this.getLocalName());
        super.takeDown();
    }
}
