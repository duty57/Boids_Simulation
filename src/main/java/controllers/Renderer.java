package controllers;

import com.jogamp.opengl.GL4;
import models.Simulation;
import models.Wind;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Objects;

public class Renderer {
    private static Renderer instance;
    private final Simulation simulation;
    private final ShaderController shaderController;
    private final RenderInitializer renderInitializer;
    private final Wind wind = new Wind();
    private final Matrix4f transformationMatrix = new Matrix4f();
    private final Vector3f translation = new Vector3f();
    private final Object windLock = new Object();
    private Renderer(Simulation simulation) {
        this.simulation = Objects.requireNonNull(simulation);
        shaderController = ShaderController.getInstance(Objects.requireNonNull(simulation));
        renderInitializer = RenderInitializer.getInstance();
    }

    public static Renderer getInstance(Simulation simulation) {
        if (instance == null) {
            instance = new Renderer(simulation);
        }
        return instance;
    }

    private void initSsbo(GL4 gl) {
        renderInitializer.initSsbo(gl, simulation);
    }

    public void initBoids() {
        renderInitializer.initBoids(simulation);
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

    public void initOpenGL(GL4 gl) {

        gl.glEnable(GL4.GL_DEBUG_OUTPUT);
        gl.glEnable(GL4.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        gl.glEnable(GL4.GL_BLEND);
        gl.glBlendFunc(GL4.GL_SRC_ALPHA, GL4.GL_ONE_MINUS_SRC_ALPHA);

        int computeShader = shaderController.compileShader(gl, "boidShader.comp", GL4.GL_COMPUTE_SHADER);
        int vertexShader = shaderController.compileShader(gl, "boidShader.vert", GL4.GL_VERTEX_SHADER);
        int fragmentShader = shaderController.compileShader(gl, "boidShader.frag", GL4.GL_FRAGMENT_SHADER);
        int windVertexShader = shaderController.compileShader(gl, "windShader.vert", GL4.GL_VERTEX_SHADER);
        int windFragmentShader = shaderController.compileShader(gl, "windShader.frag", GL4.GL_FRAGMENT_SHADER);

        int computeProgram = shaderController.createComputeProgram(gl, List.of(computeShader));
        simulation.setComputeProgram(computeProgram);

        int renderProgram = shaderController.createRenderProgram(gl, List.of(vertexShader, fragmentShader));
        simulation.setRenderProgram(renderProgram);

        int windProgram = shaderController.createRenderProgram(gl, List.of(windVertexShader, windFragmentShader));
        wind.setRenderProgram(windProgram);

        shaderController.deleteShaders(gl, List.of(vertexShader, fragmentShader, windVertexShader, windFragmentShader, computeShader));
        initSsbo(gl);

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
        shaderController.updateLightningUniforms(gl);

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

        gl.glUseProgram(simulation.getComputeProgram());
        shaderController.updateUniforms(gl);

        startWindUpdateThread();
    }

    public void update(GL4 gl, float deltaTime) {

        wind.setDeltaTime(deltaTime);

        gl.glUseProgram(wind.getRenderProgram());
        applyWindData(gl);

        gl.glUseProgram(simulation.getRenderProgram());
        shaderController.updateLightningUniforms(gl);

        gl.glUseProgram(simulation.getComputeProgram());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getComputeProgram(), "deltaTime"), deltaTime);
        shaderController.updateUniforms(gl);

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
        shaderController.cleanup(gl);
    }
}
