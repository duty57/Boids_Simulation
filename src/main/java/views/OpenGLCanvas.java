package views;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import controllers.SimulationController;
import models.Simulation;

import java.nio.IntBuffer;


public class OpenGLCanvas implements GLEventListener {
    private SimulationController controller;
    private Simulation simulation;
    private long lastTime;
    private String shaderDir = "src/main/resources/shaders";

    public OpenGLCanvas(SimulationController controller, Simulation simulation) {
        this.controller = controller;
        this.simulation = simulation;
        this.lastTime = System.currentTimeMillis();
    }

    // Default constructor for testing

    @Override
    public void init(GLAutoDrawable drawable) {
        GL4 gl = drawable.getGL().getGL4();
        gl.glEnable(GL4.GL_DEPTH_TEST);
        System.out.println("OpenGL canvas initialized");
        IntBuffer buffer = IntBuffer.allocate(1);
        gl.glGetIntegeri_v(GL4.GL_MAX_COMPUTE_WORK_GROUP_SIZE, 0, buffer);
        System.out.println("Max local_size_x: " + buffer.get(0));
        System.out.println(gl.glGetString(GL.GL_VERSION));

        if (controller != null) {
            controller.initBoids();
            System.out.println("Boids initialized");
            controller.initOpenGL(gl, shaderDir);
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        if (controller != null && drawable != null) {
            GL4 gl = drawable.getGL().getGL4();
            controller.cleanup(gl);
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (controller != null && drawable != null) {
            GL4 gl = drawable.getGL().getGL4();
            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - lastTime) / 1000.0f;
            lastTime = currentTime;
            controller.update(gl, deltaTime);
            controller.render(gl);
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        if (drawable != null) {
            GL4 gl = drawable.getGL().getGL4();
            gl.glViewport(0, 0, width, height);
        }
    }
}
