import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import controllers.SimulationController;
import models.Simulation;
import views.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Simulation simulation = new Simulation();
        MainFrame mainFrame = new MainFrame();
        SimulationController simulationController = new SimulationController(mainFrame, simulation);
        simulationController.init();
    }
}