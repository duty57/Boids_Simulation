package repositories;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import models.SimulationModel;

import java.util.List;

public class SimulationRepository {

    @PersistenceContext
    private EntityManager em;

    public List<SimulationModel> findAll() {
        return em.createQuery("SELECT s FROM SimulationModel s", SimulationModel.class).getResultList();
    }

    @Transactional
    public SimulationModel add(SimulationModel sim) {
        return em.merge(sim);
    }

    @Transactional
    public SimulationModel update(SimulationModel sim) {
        return em.merge(sim);
    }

    @Transactional
    public void delete(SimulationModel sim) {
        em.remove(sim);
    }
}
