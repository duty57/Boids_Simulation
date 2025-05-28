import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import controllers.Renderer;
import controllers.SimulationController;
import models.Simulation;
import util.DatabaseInitializer;
import views.MainFrame;
import views.OpenGLCanvas;


public class Main {
    public static void main(String[] args) {

        DatabaseInitializer.initializeDatabase();

        Simulation simulation = Simulation.getInstance();
        SimulationController simulationController = SimulationController.getController(simulation);
        MainFrame mainFrame = new MainFrame(simulationController, simulation);

        GLProfile profile = GLProfile.get(GLProfile.GL4);
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setHardwareAccelerated(true);
        capabilities.setDoubleBuffered(true);
        mainFrame.init(capabilities);

        Renderer renderer = Renderer.getInstance(simulation);
        OpenGLCanvas canvas = new OpenGLCanvas(renderer);
        mainFrame.getCanvas().addGLEventListener(canvas);
        mainFrame.setVisible(true);
    }
}