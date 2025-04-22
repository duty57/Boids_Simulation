import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import controllers.SimulationController;
import models.Simulation;
import views.MainFrame;
import views.OpenGLCanvas;


public class Main {
    public static void main(String[] args) {

        Simulation simulation = new Simulation();
        SimulationController simulationController = new SimulationController(simulation);
        MainFrame mainFrame = new MainFrame(simulationController, simulation);

        GLProfile profile = GLProfile.get(GLProfile.GL4);
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setHardwareAccelerated(true);
        capabilities.setDoubleBuffered(true);
        mainFrame.init(capabilities);

        OpenGLCanvas canvas = new OpenGLCanvas(simulationController, simulation);
        mainFrame.getCanvas().addGLEventListener(canvas);
        mainFrame.setVisible(true);
    }
}