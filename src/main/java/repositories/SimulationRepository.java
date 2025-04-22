package repositories;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import models.SimModel;

import java.util.List;

public class SimulationRepository {

    @PersistenceContext
    private EntityManager em;

    public List<SimModel> findAll() {
        return em.createQuery("SELECT s FROM SimModel s", SimModel.class).getResultList();
    }

    @Transactional
    public SimModel add(SimModel sim) {
        return em.merge(sim);
    }

    @Transactional
    public SimModel update(SimModel sim) {
        return em.merge(sim);
    }

    @Transactional
    public void delete(SimModel sim) {
        em.remove(sim);
    }
}
