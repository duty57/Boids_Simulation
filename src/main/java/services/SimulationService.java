package services;

import models.SimModel;
import repositories.SimulationRepository;

import java.util.List;

public class SimulationService {

    private SimulationRepository simulationRepository;

    SimulationService(SimModel sim) {
        this.simulationRepository = new SimulationRepository();
    }

    public List<SimModel> getSimulations() {
        return simulationRepository.findAll();
    }

    public SimModel addSimulation(SimModel sim) {
        return simulationRepository.add(sim);
    }

    public SimModel updateSimulation(SimModel sim) {
        return simulationRepository.update(sim);
    }

    public void deleteSimulation(SimModel sim) {
        simulationRepository.delete(sim);
    }
}
