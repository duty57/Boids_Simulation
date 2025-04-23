package controllers;

import clients.WeatherAPIClient;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.awt.GLCanvas;
import models.SimModel;
import models.Simulation;
import models.SimulationCard;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import services.SimulationService;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.FloatBuffer;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


public class SimulationController {

    Simulation simulation;
    private final SimulationService simulationService = new SimulationService();
    private final WeatherAPIClient weatherClient = new WeatherAPIClient();

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
        try {
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
                try {
                    List<SimModel> simulation = get();
                    callback.accept(simulation);
                } catch (Exception e) {
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
            protected Void doInBackground(){
                simulationService.deleteSimulation(simCard);
                loadSimulations(historyListModel);
                return null;
            }


        }.execute();
    }

    public SimModel importSimulationData(SimulationCard simCard) {
        return simulationService.getSimulationById(simCard.getId());
    }

    public void importSliderValues(JPanel simulationPanel, SimModel importedModel) {
        for (Component comp : simulationPanel.getComponents()) {
            if (comp instanceof JPanel sliderPanel) {
                Component[] components = sliderPanel.getComponents();
                JLabel label = null;
                JSlider slider = null;
                JLabel valueLabel = null;

                for (Component c : components) {
                    if (c instanceof JLabel) {
                        if (label == null) {
                            label = (JLabel) c;
                        } else {
                            valueLabel = (JLabel) c;
                        }
                    } else if (c instanceof JSlider) {
                        slider = (JSlider) c;
                    }
                }

                if (label != null && slider != null && valueLabel != null) {
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
                        case "Wind Direction" -> (int) (importedModel.getWindDirection().floatValue());
                        default -> slider.getValue();
                    };
                    slider.setValue(value);
                    valueLabel.setText(String.valueOf(value));
                }
            }
        }
    }
    public void importSliderValues(JPanel simulationPanel, Simulation simulation) {
        for (Component comp : simulationPanel.getComponents()) {
            if (comp instanceof JPanel sliderPanel) {
                updateSliderPanel(sliderPanel, label -> {
                    return switch (label) {
                        case "Max Speed" -> (int) (simulation.getMaxSpeed() * 100);
                        case "Alignment Force" -> (int) (simulation.getAlignmentForce() * 100);
                        case "Cohesion Force" -> (int) (simulation.getCohesionForce() * 100);
                        case "Separation Force" -> (int) (simulation.getSeparationForce() * 100);
                        case "Vision Range" -> (int) (simulation.getVision() * 100);
                        case "Drag Force" -> (int) (simulation.getDragForce() * 100);
                        case "Temperature" -> (int) (simulation.getTemperature());
                        case "Wind Speed" -> (int) (simulation.getWindSpeed() * 100);
                        case "Cloudiness" -> (int) (simulation.getCloudiness() * 100);
                        case "Sun Position" -> (int) (simulation.getSunAngle());
                        case "Wind Direction" -> (int) (simulation.getWindDirection());
                        default -> 0;
                    };
                });
            }
        }
    }

    private void updateSliderPanel(JPanel sliderPanel, Function<String, Integer> valueProvider) {
        Component[] components = sliderPanel.getComponents();
        JLabel label = null;
        JSlider slider = null;
        JLabel valueLabel = null;

        for (Component c : components) {
            if (c instanceof JLabel) {
                if (label == null) {
                    label = (JLabel) c;
                } else {
                    valueLabel = (JLabel) c;
                }
            } else if (c instanceof JSlider) {
                slider = (JSlider) c;
            }
        }

        if (label != null && slider != null && valueLabel != null) {
            int value = valueProvider.apply(label.getText());
            slider.setValue(value);
            valueLabel.setText(String.valueOf(value));
        }
    }

    public void getCityParameters(String cityName, Consumer<WeatherAPIClient.CityData> callback) {
        if (StringUtils.isBlank(cityName)) {
            callback.accept(null);
            return;
        }

        new SwingWorker<WeatherAPIClient.CityData, Void>() {
            @Override
            protected WeatherAPIClient.CityData doInBackground() {
                return weatherClient.getCityData(cityName);
            }

            @Override
            protected void done() {
                try {
                    callback.accept(ObjectUtils.defaultIfNull(get(), null));

                } catch (Exception e) {
                    System.err.println("Error fetching city data: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void setupMouseListeners(GLCanvas canvas) {
        canvas.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseMoved(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                float x = (2.0f * e.getX()) / canvas.getWidth() - 1.0f;
                float y = 1.0f - (2.0f * e.getY()) / canvas.getHeight();

                GL4 gl = canvas.getGL().getGL4();
                updateMousePosition(gl, x, y);
            }
        });

        canvas.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("mousePressed");
                GL4 gl = canvas.getGL().getGL4();
                if (e.getButton() == MouseEvent.BUTTON1) {
                    setMoveTowardsMouse(gl, 1);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    setMoveAwayFromMouse(gl, 1);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("mouseReleased");
                GL4 gl = canvas.getGL().getGL4();
                if (e.getButton() == MouseEvent.BUTTON1) {
                    setMoveTowardsMouse(gl, 0);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    setMoveAwayFromMouse(gl, 0);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }
}
