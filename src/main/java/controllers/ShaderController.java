package controllers;

import com.jogamp.opengl.GL4;
import models.Simulation;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;

public class ShaderController {
    private static ShaderController instance;
    private final Simulation simulation;
    private ShaderController(Simulation simulation) {
        this.simulation = Objects.requireNonNull(simulation);
    }
    public static ShaderController getInstance(Simulation simulation) {
        if (instance == null) {
            instance = new ShaderController(simulation);
        }
        return instance;
    }

    public void updateUniforms(GL4 gl) {
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getComputeProgram(), "maxSpeed"), simulation.getMaxSpeed());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getComputeProgram(), "dragForce"), simulation.getDragForce());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getComputeProgram(), "alignmentForce"), simulation.getAlignmentForce());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getComputeProgram(), "cohesionForce"), simulation.getCohesionForce());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getComputeProgram(), "separationForce"), simulation.getSeparationForce());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getComputeProgram(), "boidVision"), simulation.getVision());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getComputeProgram(), "dragRadius"), simulation.getDragRadius());
        gl.glUniform2fv(gl.glGetUniformLocation(simulation.getComputeProgram(), "mousePosition"), 1, FloatBuffer.wrap(simulation.getMousePosition()));
        gl.glUniform1i(gl.glGetUniformLocation(simulation.getComputeProgram(), "moveTowardsMouse"), simulation.getMoveTowardsMouse());
        gl.glUniform1i(gl.glGetUniformLocation(simulation.getComputeProgram(), "moveAwayFromMouse"), simulation.getMoveAwayFromMouse());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getComputeProgram(), "windSpeed"), simulation.getWindSpeed());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getComputeProgram(), "windDirection"), simulation.getWindDirection()); //meteorological degrees
    }

    public void updateLightningUniforms(GL4 gl) {
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getRenderProgram(), "temperature"), simulation.getTemperature());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getRenderProgram(), "cloudiness"), simulation.getCloudiness());
        gl.glUniform1f(gl.glGetUniformLocation(simulation.getRenderProgram(), "sunAngle"), simulation.getSunAngle());

    }

    public int compileShader(GL4 gl, String resourcePath, int shaderType) {
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

    public int createRenderProgram(GL4 gl, List<Integer> shaders){
        int program = gl.glCreateProgram();
        for (Integer shader : shaders) {
            gl.glAttachShader(program, shader);
        }
        gl.glLinkProgram(program);
        checkLinking(gl, program);
        return program;
    }

    public int createComputeProgram(GL4 gl, List<Integer> shaders){
        int program = gl.glCreateProgram();
        for (Integer shader : shaders) {
            gl.glAttachShader(program, shader);
        }
        gl.glLinkProgram(program);
        checkLinking(gl, program);
        return program;
    }

    public void deleteShaders(GL4 gl, List<Integer> shaders){
        for (Integer shader : shaders) {
            gl.glDeleteShader(shader);
        }
    }


    public void cleanup(GL4 gl) {
        gl.glDeleteProgram(simulation.getComputeProgram());
        gl.glDeleteProgram(simulation.getRenderProgram());
        gl.glDeleteBuffers(1, simulation.getSsbo(), 0);
        gl.glDeleteVertexArrays(1, IntBuffer.wrap(new int[]{simulation.getVao()}).array(), 0);
    }

}
