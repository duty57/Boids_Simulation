package controllers;

import clients.WeatherAPIClient;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLProfile;
import models.Boid;
import models.SimModel;
import models.Simulation;
import org.joml.Vector4f;
import services.SimulationService;
import views.MainFrame;
import com.jogamp.opengl.GLCapabilities;
import views.OpenGLCanvas;

import javax.swing.*;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Random;


public class SimulationController {

    Simulation simulation;
    private SimulationService simulationService = new SimulationService();

    public SimulationController(Simulation simulation) {
        this.simulation = simulation;
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

    public SimModel saveSimulation() {
        try{
            SimModel simModel = new SimModel();
            simModel.setUser(System.getProperty("user.name") + "_" + InetAddress.getLocalHost().getHostName());
            simModel.setSimulationDate(LocalDate.now());
            simModel.setMaxSpeed(BigDecimal.valueOf(simulation.getMaxSpeed()));
            simModel.setAligmentForce(BigDecimal.valueOf(simulation.getAlignmentForce()));
            simModel.setCohesionForce(BigDecimal.valueOf(simulation.getCohesionForce()));
            simModel.setSeparationForce(BigDecimal.valueOf(simulation.getSeparationForce()));
            simModel.setBoidVision(BigDecimal.valueOf(simulation.getVision()));
            simModel.setDragForce(BigDecimal.valueOf(simulation.getDragForce()));
            simModel.setDragRadius(BigDecimal.valueOf(simulation.getDragRadius()));
            simModel.setTemperature(BigDecimal.valueOf(simulation.getTemperature()));
            simModel.setWindSpeed(BigDecimal.valueOf(simulation.getWindSpeed()));
            simModel.setWindDirection(BigDecimal.valueOf(simulation.getWindDirection()));
            simModel.setSunAngle(BigDecimal.valueOf(simulation.getSunAngle()));
            simModel.setClouds(BigDecimal.valueOf(simulation.getCloudiness()));

            return simulationService.addSimulation(simModel);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

}
