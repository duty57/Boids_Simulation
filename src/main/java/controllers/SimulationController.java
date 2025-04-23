package controllers;

import clients.WeatherAPIClient;
import com.jogamp.opengl.GL4;
import models.SimModel;
import models.Simulation;
import models.SimulationCard;
import services.SimulationService;

import javax.swing.*;

import java.awt.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.FloatBuffer;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;


public class SimulationController {

    Simulation simulation;
    private final SimulationService simulationService = new SimulationService();
    private WeatherAPIClient weatherClient = new WeatherAPIClient();

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

    public SimModel saveSimulation(DefaultListModel<SimulationCard> historyListModel) {
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

            SimModel newModel = simulationService.addSimulation(simModel);
            loadSimulations(historyListModel);
            return newModel;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    public void getSimulationsFromDB(Consumer<List<SimModel>> callback) {
        new SwingWorker<List<SimModel>, Void>() {

            @Override
            protected List<SimModel> doInBackground() throws Exception {
                return simulationService.getSimulations();
            }

            @Override
            protected void done() {
                try{
                    List<SimModel> simulation = get();
                    callback.accept(simulation);
                }catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }.execute();

    }

    public void loadSimulations(DefaultListModel<SimulationCard> historyListModel) {
        getSimulationsFromDB(simulations -> {
            historyListModel.clear();
            for (SimModel simModel : simulations) {
                historyListModel.addElement(new SimulationCard(
                        simModel.getId(),
                        simModel.getSimulationDate(),
                        simModel.getBoidVision(),
                        simModel.getTemperature(),
                        simModel.getMaxSpeed()
                ));
            }
        });
    }
    public void deleteSimulation(SimulationCard simCard, DefaultListModel<SimulationCard> historyListModel) {
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                simulationService.deleteSimulation(simCard);
                loadSimulations(historyListModel);
                return null;
            }


        }.execute();
    }
    public SimModel importSimulationData(SimulationCard simCard) {
        return simulationService.getSimulationById(simCard.getId());
    }
    public void updateSliderValues(int value, String label) {
        float normalizedValue = value / 100.0f;
        switch (label) {
            case "Max Speed" -> simulation.setMaxSpeed(normalizedValue);
            case "Alignment Force" -> simulation.setAlignmentForce(normalizedValue);
            case "Cohesion Force" -> simulation.setCohesionForce(normalizedValue);
            case "Separation Force" -> simulation.setSeparationForce(normalizedValue);
            case "Vision Range" -> simulation.setVision(normalizedValue);
            case "Drag Force" -> simulation.setDragForce(normalizedValue);
            case "Temperature" -> simulation.setTemperature((value + 50.0f) / 100.0f);//remake
            case "Wind Speed" -> simulation.setWindSpeed(normalizedValue);
            case "Cloudiness" -> simulation.setCloudiness(normalizedValue);
            case "Sun Position" -> simulation.setSunAngle(value);
        }
    }
    public void importSliderValues(JPanel simulationPanel, SimModel importedModel) {
        // Update all sliders
        for (Component comp : simulationPanel.getComponents()) {
            if (comp instanceof JPanel sliderPanel) {
                JLabel label = (JLabel) ((BorderLayout) sliderPanel.getLayout()).getLayoutComponent(BorderLayout.NORTH);
                JSlider slider = (JSlider) ((BorderLayout) sliderPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                JLabel valueLabel = (JLabel) ((BorderLayout) sliderPanel.getLayout()).getLayoutComponent(BorderLayout.EAST);

                if (label != null && slider != null) {
                    int value = switch (label.getText()) {
                        case "Max Speed" -> (int) (importedModel.getMaxSpeed().floatValue() * 100);
                        case "Alignment Force" -> (int) (importedModel.getAligmentForce().floatValue() * 100);
                        case "Cohesion Force" -> (int) (importedModel.getCohesionForce().floatValue() * 100);
                        case "Separation Force" -> (int) (importedModel.getSeparationForce().floatValue() * 100);
                        case "Vision Range" -> (int) (importedModel.getBoidVision().floatValue() * 100);
                        case "Drag Force" -> (int) (importedModel.getDragForce().floatValue() * 100);
                        case "Temperature" -> (int) (importedModel.getTemperature().floatValue());
                        case "Wind Speed" -> (int) (importedModel.getWindSpeed().floatValue() * 100);
                        case "Cloudiness" -> (int) (importedModel.getClouds().floatValue() * 100);
                        case "Sun Position" -> (int) (importedModel.getSunAngle().floatValue());
                        default -> slider.getValue();
                    };
                    slider.setValue(value);
                    valueLabel.setText(String.valueOf(value));
                }
            }
        }
    }
    public WeatherAPIClient.CityData getCityParameters(String cityName) {
        return weatherClient.getCityData(cityName);
    }
}
