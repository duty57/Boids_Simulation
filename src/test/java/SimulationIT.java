import models.SimModel;
import org.junit.jupiter.api.Test;
import services.SimulationService;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SimulationIT {
    private SimulationService simulationService = new SimulationService();
    @Test
    public void saveData() {

        SimModel simModel = new SimModel();
        simModel.setMaxSpeed(BigDecimal.valueOf(0.3));
        simModel.setUser("testUser");
        simModel.setSimulationDate(LocalDate.now());
        simModel.setDragForce(BigDecimal.valueOf(0.3));
        simModel.setDragRadius(BigDecimal.valueOf(0.3));
        simModel.setAligmentForce(BigDecimal.valueOf(0.1));
        simModel.setCohesionForce(BigDecimal.valueOf(0.1));
        simModel.setSeparationForce(BigDecimal.valueOf(0.1));
        simModel.setBoidVision(BigDecimal.valueOf(0.1));
        simModel.setWindDirection(BigDecimal.valueOf(0));
        simModel.setWindSpeed(BigDecimal.valueOf(0));
        simModel.setClouds(BigDecimal.valueOf(0.8));
        simModel.setSunAngle(BigDecimal.valueOf(0));
        simModel.setTemperature(BigDecimal.valueOf(10));

        SimModel response = simulationService.addSimulation(simModel);
        assertNotNull(response);
    }

    @Test
    public void getData() throws UnknownHostException {
        List<SimModel> simModels = simulationService.getSimulations();
        assertNotNull(simModels);
    }

}
