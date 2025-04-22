package repositories;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import models.SimModel;

import java.util.List;

public class SimulationRepository {

    private EntityManagerFactory emf;
    private EntityManager em;

    public SimulationRepository() {
        this.emf = Persistence.createEntityManagerFactory("simulation");
        this.em = emf.createEntityManager();
    }

    public List<SimModel> findAll() {
        return em.createQuery("SELECT s FROM SimModel s", SimModel.class).getResultList();
    }

    public SimModel add(SimModel sim) {
        em.getTransaction().begin();
        SimModel result = em.merge(sim);
        em.getTransaction().commit();
        return result;
    }

    public SimModel update(SimModel sim) {
        em.getTransaction().begin();
        SimModel result = em.merge(sim);
        em.getTransaction().commit();
        return result;
    }

    public void delete(SimModel sim) {
        em.getTransaction().begin();
        em.remove(sim);
        em.getTransaction().commit();
    }
}
