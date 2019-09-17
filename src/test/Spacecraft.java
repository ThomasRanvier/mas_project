package test;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Spacecraft extends Agent {
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
