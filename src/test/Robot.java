package test;

import jade.core.Agent;

public class Robot extends Agent {
    @Override
    protected void setup(){
        System.out.println("Hello, I'm a little bot, " + this.getLocalName());
        //this.doDelete();
    }

    @Override
    protected void takeDown() {
        System.out.println("Taking down " + this.getLocalName());
        super.takeDown();
    }
}
