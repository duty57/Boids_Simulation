package models;

import lombok.Getter;
import lombok.Setter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Getter
@Setter
public class Simulation {

    private int shaderProgram;
    private int renderProgram;
    private int[] ssbo = new int[1];
    private int vao;

    private int NUMBER_OF_BOIDS = 1024;
    private float maxSpeed = 0.5f;
    private float dragForce = 0.01f;
    private float alignmentForce = 0.011f;
    private float cohesionForce = 0.01f;
    private float separationForce = 0.011f;
    private float vision = 0.01f;
    private float dragRadius = 0.05f;

    private Boid[] boids = new Boid[NUMBER_OF_BOIDS];
    private int moveTowardsMouse = 0;
    private int moveAwayFromMouse = 0;
    private float[] mousePosition = new float[2];

    public Simulation() {
        Properties prop = new Properties();
        InputStream input;
        input = Simulation.class.getClassLoader().getResourceAsStream("config/simulation.properties");

        try {
            prop.load(input);
            NUMBER_OF_BOIDS = Integer.parseInt(prop.getProperty("simulation.numberOfBoids"));
            maxSpeed = Float.parseFloat(prop.getProperty("boids.maxSpeed"));
            dragForce = Float.parseFloat(prop.getProperty("boids.dragForce"));
            alignmentForce = Float.parseFloat(prop.getProperty("boids.alignmentForce"));
            cohesionForce = Float.parseFloat(prop.getProperty("boids.cohesionForce"));
            separationForce = Float.parseFloat(prop.getProperty("boids.separationForce"));
            vision = Float.parseFloat(prop.getProperty("boids.vision"));
            dragRadius = Float.parseFloat(prop.getProperty("boids.dragRadius"));
        }catch (Exception e){
            System.out.println("Error loading shader program");
        }


    }

    public int getNumberOfBoids() {
        return this.NUMBER_OF_BOIDS;
    }
}
