package Test;

import jade.core.Agent;

public class robot extends Agent {
    protected void setup(){
        System.out.println("Hello, my name is " + this.getLocalName());
    }
}
