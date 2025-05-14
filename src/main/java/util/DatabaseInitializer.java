package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.File;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        File database = new File("boids.db");
        if (!database.exists()) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("simulation");
            EntityManager em = emf.createEntityManager();

            em.close();
            emf.close();
        }
    }
}
