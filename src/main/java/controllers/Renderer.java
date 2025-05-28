package controllers;

import com.jogamp.opengl.GL4;
import models.Boid;
import models.Simulation;
import models.Wind;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.Random;

public class Renderer {
    private static Renderer instance;
    private final Simulation simulation;
    private final Wind wind = new Wind();
    private final Matrix4f transformationMatrix = new Matrix4f();
    private final Vector3f translation = new Vector3f();
    private final Object windLock = new Object();
    private Renderer(Simulation simulation) {
        this.simulation = simulation;
    }

    public static Renderer getInstance(Simulation simulation) {
        if (instance == null) {
            instance = new Renderer(simulation);
        }
        return instance;
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
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getShaderProgram(), "windSpeed"), simulation.getWindSpeed());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getShaderProgram(), "windDirection"), simulation.getWindDirection()); //meteorological degrees
    }

    private void updateLightningUniforms(GL4 gl) {
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getRenderProgram(), "temperature"), simulation.getTemperature());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getRenderProgram(), "cloudiness"), simulation.getCloudiness());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getRenderProgram(), "sunAngle"), simulation.getSunAngle());

    }

    private void startWindUpdateThread() {
        Thread windThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                updateWindData();
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "WindUpdateThread");
        windThread.setDaemon(true);
        windThread.start();
    }

    private void updateWindData() {
        float angle = (float) Math.toRadians((450 - simulation.getWindDirection()) % 360);
        synchronized (windLock) {
            translation.add(
                    0.5f * (float) Math.cos(angle) * wind.getDeltaTime(),
                    0.5f * (float) Math.sin(angle) * wind.getDeltaTime(),
                    0.0f
            );
            transformationMatrix.identity()
                    .translate(translation)
                    .rotate(angle, 0, 0, 1)
                    .scale(1.0f);

            if (Math.abs(translation.x) > 2.0f || Math.abs(translation.y) > 2.0f) {
                translation.x = 0.0f;
                translation.y = 0.0f;
            }
        }
    }

    private void applyWindData(GL4 gl) {
        synchronized (windLock) {
            gl.glUniform1f(gl.glGetUniformLocation(wind.getRenderProgram(), "windSpeed"), simulation.getWindSpeed());
            gl.glUniformMatrix4fv(gl.glGetUniformLocation(wind.getRenderProgram(), "transformationMatrix"), 1, false, transformationMatrix.get(new float[16]), 0);
        }
    }

    private int compileShader(GL4 gl, String resourcePath, int shaderType) {
        if (StringUtils.isBlank(resourcePath)) return -1;

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
            gl.glGetProgramInfoLog(program, logLength[0], null, 0, log, 0);

            String errorMessage = new String(log);
            System.err.println("Linking failed: " + errorMessage);
        }
    }

    public void initOpenGL(GL4 gl) {

        gl.glEnable(GL4.GL_DEBUG_OUTPUT);
        gl.glEnable(GL4.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        gl.glEnable(GL4.GL_BLEND);
        gl.glBlendFunc(GL4.GL_SRC_ALPHA, GL4.GL_ONE_MINUS_SRC_ALPHA);

        int computeShader = compileShader(gl, "boidShader.comp", GL4.GL_COMPUTE_SHADER);
        int vertexShader = compileShader(gl, "boidShader.vert", GL4.GL_VERTEX_SHADER);
        int fragmentShader = compileShader(gl, "boidShader.frag", GL4.GL_FRAGMENT_SHADER);

        int windVertexShader = compileShader(gl, "windShader.vert", GL4.GL_VERTEX_SHADER);
        int windFragmentShader = compileShader(gl, "windShader.frag", GL4.GL_FRAGMENT_SHADER);

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

// create wind shader program
        int windProgram = gl.glCreateProgram();
        wind.setRenderProgram(windProgram);
        gl.glAttachShader(windProgram, windVertexShader);
        gl.glAttachShader(windProgram, windFragmentShader);
        gl.glLinkProgram(windProgram);
        checkLinking(gl, windProgram);

        // Clean up shader
        gl.glDeleteShader(computeShader);
        gl.glDeleteShader(vertexShader);
        gl.glDeleteShader(fragmentShader);

        gl.glDeleteShader(windVertexShader);
        gl.glDeleteShader(windFragmentShader);

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
        gl.glUseProgram(renderProgram);
        updateLightningUniforms(gl);

        int[] windVaoArray = new int[1];
        gl.glGenVertexArrays(1, windVaoArray, 0);
        wind.setVao(windVaoArray[0]);
        gl.glBindVertexArray(windVaoArray[0]);

        int[] windVbo = new int[1];
        gl.glGenBuffers(1, windVbo, 0);
        gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, windVbo[0]);

        gl.glBufferData(GL4.GL_ARRAY_BUFFER, wind.getVertices().length * 4L,
                FloatBuffer.wrap(wind.getVertices()), GL4.GL_STATIC_DRAW);
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL4.GL_FLOAT, false, 0, 0);


        gl.glUseProgram(simulation.getShaderProgram());
        updateUniforms(gl);

        startWindUpdateThread();
    }

    public void update(GL4 gl, float deltaTime) {

        wind.setDeltaTime(deltaTime);

        gl.glUseProgram(wind.getRenderProgram());
        applyWindData(gl);

        gl.glUseProgram(simulation.getRenderProgram());
        updateLightningUniforms(gl);

        gl.glUseProgram(simulation.getShaderProgram());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getShaderProgram(), "deltaTime"), deltaTime);
        updateUniforms(gl);

        //dispatch compute shader
        gl.glDispatchCompute((simulation.getNUMBER_OF_BOIDS() + 255) / 256, 1, 1);
        gl.glMemoryBarrier(GL4.GL_SHADER_STORAGE_BARRIER_BIT);
    }

    public void render(GL4 gl) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(simulation.getRenderProgram());
        gl.glMemoryBarrier(GL4.GL_SHADER_STORAGE_BARRIER_BIT | GL4.GL_BUFFER_UPDATE_BARRIER_BIT);
        gl.glBindVertexArray(simulation.getVao());
        gl.glDrawArraysInstanced(GL4.GL_TRIANGLE_STRIP, 0, 4, simulation.getNUMBER_OF_BOIDS());

        gl.glLineWidth(1.0f);
        gl.glUseProgram(wind.getRenderProgram());
        gl.glBindVertexArray(wind.getVao());
        gl.glDrawArrays(GL4.GL_LINES, 0, wind.getVertices().length / 3);

    }

    public void cleanup(GL4 gl) {
        gl.glDeleteProgram(simulation.getShaderProgram());
        gl.glDeleteProgram(simulation.getRenderProgram());
        gl.glDeleteBuffers(1, simulation.getSsbo(), 0);
        gl.glDeleteVertexArrays(1, IntBuffer.wrap(new int[]{simulation.getVao()}).array(), 0);
    }
}
