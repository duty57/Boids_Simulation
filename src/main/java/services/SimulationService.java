package services;

import models.SimModel;
import models.SimulationCard;
import repositories.SimulationRepository;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class SimulationService {
    private static SimulationService instance;
    private final SimulationRepository simulationRepository;

    private SimulationService() {
        this.simulationRepository = SimulationRepository.getRepository();
    }

    public static SimulationService getService() {
        if (instance == null) {
            instance = new SimulationService();
        }
        return instance;
    }

    public List<SimModel> getSimulations() throws UnknownHostException {
        return simulationRepository.findAll(System.getProperty("user.name") + "_" + InetAddress.getLocalHost().getHostName());
    }

    public SimModel addSimulation(SimModel sim) {
        return simulationRepository.add(sim);
    }

    public void deleteSimulation(SimulationCard sim) {
        simulationRepository.deleteById(sim.getId());
    }

    public SimModel getSimulationById(Long id) {
        return simulationRepository.findById(id);
    }
}
