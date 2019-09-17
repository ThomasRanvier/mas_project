package test;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.Objects;

public class World {
    public static void main(String[] args){
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.GUI, "true");
        ContainerController containerController = runtime.createMainContainer(profile);

        Object[] spaceCraftArgs = {containerController, 5};
        AgentController spacecraftController;
        try {
            spacecraftController = containerController.createNewAgent("TheBoss", "test.Spacecraft", spaceCraftArgs);
            spacecraftController.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

        int botsNumber = 4;
        for(int i = 1; i <= botsNumber; i++){
            AgentController botsController;
            try {
                botsController = containerController.createNewAgent("bot_" + i, "test.Robot", null);
                botsController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }
    }
}
