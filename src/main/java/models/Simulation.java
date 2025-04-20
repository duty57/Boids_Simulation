package models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Simulation {

    private int shaderProgram;
    private int renderProgram;
    private int[] ssbo = new int[1];
    private int vao;

    private final int NUMBER_OF_BOIDS = 1024;

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

    public Simulation() {}

    public int getNumberOfBoids() {
        return this.NUMBER_OF_BOIDS;
    }
}
