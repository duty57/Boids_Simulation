import controllers.Renderer;
import models.Simulation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RendererTest {

    @Test
    public void testDataSetup() {
        Simulation simulation = new Simulation();
        Renderer renderer = new Renderer(simulation);

        renderer.initBoids();
        assertNotNull(simulation.getBoids());
    }

}
