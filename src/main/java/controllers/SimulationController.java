package controllers;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLProfile;
import models.Boid;
import models.Simulation;
import org.joml.Vector4f;
import views.MainFrame;
import com.jogamp.opengl.GLCapabilities;
import views.OpenGLCanvas;

import javax.swing.*;

import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Random;


public class SimulationController {

    MainFrame mainFrame;
    Simulation simulation;

    public SimulationController(MainFrame mainFrame, Simulation simulation) {
        this.mainFrame = mainFrame;
        this.simulation = simulation;

    }

    public void init() {

        GLProfile profile = GLProfile.get(GLProfile.GL4);
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setHardwareAccelerated(true);
        capabilities.setDoubleBuffered(true);

        this.mainFrame.init(capabilities);
        OpenGLCanvas canvas = new OpenGLCanvas(this, simulation);
        this.mainFrame.getCanvas().addGLEventListener(canvas);
        this.mainFrame.setVisible(true);

    }

    public void initBoids() {
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

    private int compileShader(GL4 gl, String resourcePath, int shaderType) {
        int shaderId = gl.glCreateShader(shaderType);

        try {
            String shaderSource = new String(
                    Objects.requireNonNull(SimulationController.class.getClassLoader()
                                    .getResourceAsStream("shaders/" + resourcePath))
                            .readAllBytes()
            );
            gl.glShaderSource(shaderId, 1, new String[]{shaderSource}, null);
            gl.glCompileShader(shaderId);

            // Check compilation status
            int[] status = new int[1];
            gl.glGetShaderiv(shaderId, GL4.GL_COMPILE_STATUS, status, 0);
            if (status[0] == GL4.GL_FALSE) {
                // Get the length of the info log
                int[] length = new int[1];
                gl.glGetShaderiv(shaderId, GL4.GL_INFO_LOG_LENGTH, length, 0);

                // Allocate buffer for the info log
                byte[] infoLog = new byte[length[0]];
                gl.glGetShaderInfoLog(shaderId, length[0], length, 0, infoLog, 0);

                String errorMessage = new String(infoLog);
                System.err.println("Shader compilation error: " + errorMessage);
                gl.glDeleteShader(shaderId);
                return 0;
            }

            return shaderId;
        } catch (IOException e) {
            System.err.println("Failed to read shader file: " + resourcePath);
            gl.glDeleteShader(shaderId);
            return 0;
        }
    }

    private void checkLinking(GL4 gl, int program) {
        int[] linked = new int[1];
        gl.glGetProgramiv(program, GL4.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 0) {
            int[] logLength = new int[1];
            gl.glGetProgramiv(program, GL4.GL_INFO_LOG_LENGTH, logLength, 0);
            byte[] log = new byte[logLength[0]];
            gl.glGetProgramInfoLog(program, logLength[0], (int[]) null, 0, log, 0);

            String errorMessage = new String(log);
            System.err.println("Linking failed: " + errorMessage);
        }
    }

    public void cleanup(GL4 gl) {
        gl.glDeleteProgram(simulation.getShaderProgram());
        gl.glDeleteProgram(simulation.getRenderProgram());
        gl.glDeleteBuffers(1, simulation.getSsbo(), 0);
        gl.glDeleteVertexArrays(1, IntBuffer.wrap(new int[]{simulation.getVao()}).array(), 0);
    }

    private void initSsbo(GL4 gl) {
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

    public void initOpenGL(GL4 gl, String shaderDir) {

        gl.glEnable(GL4.GL_DEBUG_OUTPUT);
        gl.glEnable(GL4.GL_DEBUG_OUTPUT_SYNCHRONOUS);

        int computeShader = compileShader(gl, "boidShader.comp", GL4.GL_COMPUTE_SHADER);
        int vertexShader = compileShader(gl, "boidShader.vert", GL4.GL_VERTEX_SHADER);
        int fragmentShader = compileShader(gl, "boidShader.frag", GL4.GL_FRAGMENT_SHADER);

// create compute shader program
        int computeProgram = gl.glCreateProgram();
        simulation.setShaderProgram(computeProgram);
        gl.glAttachShader(computeProgram, computeShader);
        gl.glLinkProgram(computeProgram);
        checkLinking(gl, computeProgram);

// create render shader program
        int renderProgram = gl.glCreateProgram();
        simulation.setRenderProgram(renderProgram);
        gl.glAttachShader(renderProgram, vertexShader);
        gl.glAttachShader(renderProgram, fragmentShader);
        gl.glLinkProgram(renderProgram);
        checkLinking(gl, renderProgram);

        // Clean up shader
        gl.glDeleteShader(computeShader);
        gl.glDeleteShader(vertexShader);
        gl.glDeleteShader(fragmentShader);

        initSsbo(gl);
        System.out.println("Ssbo size: " + simulation.getSsbo().length);

        int[] vaoArray = new int[1];
        gl.glGenVertexArrays(1, vaoArray, 0);
        simulation.setVao(vaoArray[0]);
        gl.glBindVertexArray(vaoArray[0]);

        // Create VBO for boid shape
        int[] vbo = new int[1];
        gl.glGenBuffers(1, vbo, 0);
        gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, vbo[0]);

        float[] vertices = {
                0.0125f, -0.025f,  // Triangle strip for boid shape
                0.0f, 0.0125f,
                0.0f, -0.0125f,
                -0.0125f, -0.025f
        };

        gl.glBufferData(GL4.GL_ARRAY_BUFFER, vertices.length * 4,
            FloatBuffer.wrap(vertices), GL4.GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 2, GL4.GL_FLOAT, false, 0, 0);

        // Set initial uniform values
        gl.glUseProgram(simulation.getShaderProgram());
        updateUniforms(gl);
    }

    public void update(GL4 gl, float deltaTime) {
        gl.glUseProgram(simulation.getShaderProgram());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getShaderProgram(), "deltaTime"), deltaTime);
        updateUniforms(gl);

        //dispatch compute shader
        gl.glDispatchCompute((simulation.getNUMBER_OF_BOIDS() + 255) / 256, 1, 1);
        gl.glMemoryBarrier(GL4.GL_SHADER_STORAGE_BARRIER_BIT);
    }

    private void updateUniforms(GL4 gl) {
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getShaderProgram(), "maxSpeed"), simulation.getMaxSpeed());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getShaderProgram(), "dragForce"), simulation.getDragForce());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getShaderProgram(), "alignmentForce"), simulation.getAlignmentForce());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getShaderProgram(), "cohesionForce"), simulation.getCohesionForce());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getShaderProgram(), "separationForce"), simulation.getSeparationForce());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getShaderProgram(), "boidVision"), simulation.getVision());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getShaderProgram(), "dragRadius"), simulation.getDragRadius());

        gl.glUniform2fv(gl.glGetUniformLocation(simulation.getShaderProgram(), "mousePosition"), 1, FloatBuffer.wrap(simulation.getMousePosition()));
        gl.glUniform1i(gl.glGetUniformLocation(simulation.getShaderProgram(), "moveTowardsMouse"), simulation.getMoveTowardsMouse());
        gl.glUniform1i(gl.glGetUniformLocation(simulation.getShaderProgram(), "moveAwayFromMouse"), simulation.getMoveAwayFromMouse());
    }

    public void render(GL4 gl) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);
        gl.glUseProgram(simulation.getRenderProgram());
        gl.glMemoryBarrier(GL4.GL_SHADER_STORAGE_BARRIER_BIT | GL4.GL_BUFFER_UPDATE_BARRIER_BIT);
        gl.glBindVertexArray(simulation.getVao());
        gl.glDrawArraysInstanced(GL4.GL_TRIANGLE_STRIP, 0, 4, simulation.getNUMBER_OF_BOIDS());
    }

    public void updateMousePosition(GL4 gl, float x, float y) {
        simulation.setMousePosition(new float[]{x, y});

        gl.glUseProgram(simulation.getShaderProgram());
        gl.glUniform2fv(gl.glGetUniformLocation(simulation.getShaderProgram(), "mousePosition"), 1, FloatBuffer.wrap(simulation.getMousePosition()));
    }

    public void setMoveTowardsMouse(GL4 gl, int value) {
        simulation.setMoveTowardsMouse(value);
        gl.glUseProgram(simulation.getShaderProgram());
        gl.glUniform1i(gl.glGetUniformLocation(simulation.getShaderProgram(), "moveTowardsMouse"), value);
    }

    public void setMoveAwayFromMouse(GL4 gl, int value) {
        simulation.setMoveAwayFromMouse(value);
        gl.glUseProgram(simulation.getShaderProgram());
        gl.glUniform1i(gl.glGetUniformLocation(simulation.getShaderProgram(), "moveAwayFromMouse"), value);
    }


}
