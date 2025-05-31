package controllers;

import com.jogamp.opengl.GL4;
import models.Boid;
import models.Simulation;
import org.joml.Vector4f;

import java.nio.FloatBuffer;
import java.util.Random;

public class RenderInitializer {
    private static RenderInitializer instance;

    private RenderInitializer() {}
    public static RenderInitializer getInstance() {
        if (instance == null) {
            instance = new RenderInitializer();
        }
        return instance;
    }

    public void initSsbo(GL4 gl, Simulation simulation) {
        // create SSBO
        gl.glGenBuffers(1, simulation.getSsbo(), 0);
        gl.glBindBuffer(GL4.GL_SHADER_STORAGE_BUFFER, simulation.getSsbo()[0]);

        int structSize = 48;
        int bufferSize = simulation.getNUMBER_OF_BOIDS() * structSize;
        FloatBuffer boidData = FloatBuffer.allocate(bufferSize / 4);

        System.out.println("Boids size: " + simulation.getBoids().length);
        for (Boid boid : simulation.getBoids()) {
            Vector4f position = boid.getPosition();
            Vector4f velocity = boid.getVelocity();
            // Position (vec4)
            boidData.put(new float[] {position.x, position.y, position.z, position.w});
            // Velocity (vec4)
            boidData.put(new float[] {velocity.x, velocity.y, velocity.z, velocity.x});
            // Angle (float)
            boidData.put(boid.getAngle());
        }
        boidData.flip();

        gl.glBufferData(GL4.GL_SHADER_STORAGE_BUFFER, bufferSize, boidData, GL4.GL_STATIC_DRAW);
        gl.glBindBufferBase(GL4.GL_SHADER_STORAGE_BUFFER, 0, simulation.getSsbo()[0]);
    }

    public void initBoids(Simulation simulation) {
        Random positionRandom = new Random();
        Random velocityRandom = new Random();
        Random angleRandom = new Random();

        for (int i = 0; i < simulation.getNumberOfBoids(); i++) {
            Vector4f position = new Vector4f(positionRandom.nextFloat(), positionRandom.nextFloat(), 0.0f, 1.0f);
            Vector4f velocity = new Vector4f(velocityRandom.nextFloat(), velocityRandom.nextFloat(), 0.0f, 1.0f);
            float angle = angleRandom.nextFloat() * 360.0f;
            simulation.getBoids()[i] = new Boid(position, velocity, angle);
        }
    }
}
