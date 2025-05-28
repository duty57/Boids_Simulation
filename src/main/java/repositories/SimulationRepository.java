package repositories;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import models.SimModel;

import java.util.List;

public class SimulationRepository {
    private static SimulationRepository instance;
    private final EntityManager em;

    private SimulationRepository() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("simulation");
        this.em = emf.createEntityManager();
    }

    public static SimulationRepository getRepository() {
        if (instance == null) {
            instance = new SimulationRepository();
        }

        return instance;
    }

    public List<SimModel> findAll(String userName) {
        return em.createQuery(
                        "SELECT s FROM SimModel s WHERE s.user = :userName ORDER BY s.simulationDate DESC",
                        SimModel.class)
                .setParameter("userName", userName)
                .getResultList();
    }

    public SimModel add(SimModel sim) {
        em.getTransaction().begin();
        SimModel result = em.merge(sim);
        em.getTransaction().commit();
        return result;
    }


    public void deleteById(Long id) {
        em.getTransaction().begin();
        SimModel sim = em.find(SimModel.class, id);
        if (sim != null) {
            em.remove(sim);
        }
        em.getTransaction().commit();
    }

    public SimModel findById(Long id) {
        em.getTransaction().begin();
        SimModel result = em.find(SimModel.class, id);
        em.getTransaction().commit();
        return result;
    }
}
