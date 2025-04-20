import controllers.SimulationController;
import models.Simulation;
import views.MainFrame;



public class Main {
    public static void main(String[] args) {
        Simulation simulation = new Simulation();
        MainFrame mainFrame = new MainFrame();
        SimulationController simulationController = new SimulationController(mainFrame, simulation);
        simulationController.init();
    }
}