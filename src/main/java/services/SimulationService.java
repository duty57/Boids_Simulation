package services;

import models.SimulationModel;
import repositories.SimulationRepository;

import java.util.List;

public class SimulationService {

    private SimulationRepository simulationRepository;

    SimulationService(SimulationModel sim) {
        this.simulationRepository = new SimulationRepository();
    }

    public List<SimulationModel> getSimulations() {
        return simulationRepository.findAll();
    }

    public SimulationModel addSimulation(SimulationModel sim) {
        return simulationRepository.add(sim);
    }

    public SimulationModel updateSimulation(SimulationModel sim) {
        return simulationRepository.update(sim);
    }

    public void deleteSimulation(SimulationModel sim) {
        simulationRepository.delete(sim);
    }
}
