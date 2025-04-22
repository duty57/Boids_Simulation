package views;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLCanvas;

import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import controllers.SimulationController;
import lombok.Getter;
import lombok.Setter;
import models.SimModel;
import models.Simulation;
import services.SimulationService;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;

@Getter
@Setter
public class MainFrame extends JFrame {

    private GLCanvas canvas;
    private Simulation simulation;
    private SimulationController simulationController;
    private SimulationService simulationService = new SimulationService();

    public MainFrame(SimulationController simulationController, Simulation simulation) {
        super("Boid Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        this.simulationController = simulationController;
        this.simulation = simulation;
    }


    // In MainFrame.java - modify the init method
    public void init(GLCapabilities capabilities) {
        // Create canvas
        canvas = new GLCanvas(capabilities);
        canvas.setPreferredSize(new Dimension(800, 400));

        // Create settings panel
        JPanel settingsPanel = createSettingsPanel();

        // Configure layout
        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        add(settingsPanel, BorderLayout.EAST);

        // Make visible
        pack();

        FPSAnimator animator = new FPSAnimator(canvas, 60);
        // Add window listener to stop animator on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (animator.isAnimating()) {
                    animator.stop();
                }
                System.exit(0);
            }
        });

        setVisible(true);
        animator.start();
        setupMouseListeners();
    }

private JPanel createSettingsPanel() {
    JPanel mainPanel = new JPanel();
    mainPanel.setPreferredSize(new Dimension(200, 600));
    mainPanel.setLayout(new BorderLayout());

    JTabbedPane tabbedPane = new JTabbedPane();

    JScrollPane simulationScroll = getSimulationPane();

    importSimulationPane(tabbedPane, simulationScroll);

    mainPanel.add(tabbedPane, BorderLayout.CENTER);
    return mainPanel;
}

    private static void importSimulationPane(JTabbedPane tabbedPane, JScrollPane simulationScroll) {
        // Search Tab
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel searchLabel = new JLabel("Enter city name:");
        searchLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField configName = new JTextField();
        configName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JPanel searchButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton searchButton = new JButton("Search");
        searchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchButtonPanel.add(searchButton);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        saveButton.setBackground(Color.getHSBColor(0.25f, 0.8f, 0.69f));
        JButton importButton = new JButton("Import");

        buttonPanel.add(saveButton);
        buttonPanel.add(importButton);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        searchPanel.add(searchLabel);
        searchPanel.add(Box.createVerticalStrut(5));
        searchPanel.add(configName);
        searchPanel.add(Box.createVerticalStrut(5));
        searchPanel.add(searchButtonPanel);
        searchPanel.add(Box.createVerticalStrut(10));
        searchPanel.add(buttonPanel);
        searchPanel.add(Box.createVerticalGlue());

        // Add tabs
        tabbedPane.addTab("Simulation", simulationScroll);
        tabbedPane.addTab("Search", searchPanel);
    }

    private JScrollPane getSimulationPane() {
        // Simulation Tab
        JPanel simulationPanel = new JPanel();
        simulationPanel.setLayout(new BoxLayout(simulationPanel, BoxLayout.Y_AXIS));

        // Add sliders for simulation parameters
        simulationPanel.add(createSliderPanel("Max Speed", 1, 100, (int) (simulation.getMaxSpeed() * 100)));
        simulationPanel.add(createSliderPanel("Alignment Force", 0, 10, (int) (simulation.getAlignmentForce() * 100)));
        simulationPanel.add(createSliderPanel("Cohesion Force", 0, 10, (int)(simulation.getCohesionForce() * 100)));
        simulationPanel.add(createSliderPanel("Separation Force", 0, 10, (int)(simulation.getSeparationForce() * 100)));
        simulationPanel.add(createSliderPanel("Vision Range", 0, 10, (int)(simulation.getVision() * 100)));
        simulationPanel.add(createSliderPanel("Drag Force", 0, 100, (int) (simulation.getDragForce() * 100)));
        simulationPanel.add(createSliderPanel("Temperature", -50, 50, (int) (simulation.getTemperature())));
        simulationPanel.add(createSliderPanel("Wind Speed", 0, 100, (int)(simulation.getWindSpeed() * 100)));
        simulationPanel.add(createSliderPanel("Cloudiness", 0, 100, (int)(simulation.getCloudiness() * 100)));
        simulationPanel.add(createSliderPanel("Sun Position", 0, 180, (int)(simulation.getSunAngle())));

        JPanel simSavePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton simSaveButton = new JButton("Save");
        simSaveButton.setBackground(Color.getHSBColor(0.25f, 0.8f, 0.69f));
        simSavePanel.add(simSaveButton);
        simulationPanel.add(Box.createVerticalStrut(10));
        simulationPanel.add(simSavePanel);

        simSaveButton.addActionListener(e -> {

        });

        JScrollPane simulationScroll = new JScrollPane(simulationPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return simulationScroll;
    }

    private JPanel createSliderPanel(String label, int min, int max, int initial) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel titleLabel = new JLabel(label);
        JSlider slider = new JSlider(min, max, initial);
        slider.setPreferredSize(new Dimension(50, 10));
        JLabel valueLabel = new JLabel(String.valueOf(initial));

        slider.addChangeListener(e -> {
            int value = slider.getValue();
            valueLabel.setText(String.valueOf(value));
            // Here you would add code to update the simulation parameters

            // Update simulation parameters based on slider label
            float normalizedValue = value / 100.0f;
            switch (label) {
                case "Max Speed" -> simulation.setMaxSpeed(normalizedValue);
                case "Alignment Force" -> simulation.setAlignmentForce(normalizedValue);
                case "Cohesion Force" -> simulation.setCohesionForce(normalizedValue);
                case "Separation Force" -> simulation.setSeparationForce(normalizedValue);
                case "Vision Range" -> simulation.setVision(normalizedValue);
                case "Drag Force" -> simulation.setDragForce(normalizedValue);
                case "Temperature" -> simulation.setTemperature(value);
                case "Wind Speed" -> simulation.setWindSpeed(normalizedValue);
                case "Cloudiness" -> simulation.setCloudiness(normalizedValue);
                case "Sun Position" -> simulation.setSunAngle(value);
            }
        });

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.EAST);

        return panel;
    }

    private void setupMouseListeners() {
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
                simulationController.updateMousePosition(gl, x, y);
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
                    simulationController.setMoveTowardsMouse(gl, 1);
                }else if (e.getButton() == MouseEvent.BUTTON3) {
                    simulationController.setMoveAwayFromMouse(gl, 1);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("mouseReleased");
                GL4 gl = canvas.getGL().getGL4();
                if (e.getButton() == MouseEvent.BUTTON1) {
                    simulationController.setMoveTowardsMouse(gl, 0);
                }else if (e.getButton() == MouseEvent.BUTTON3) {
                    simulationController.setMoveAwayFromMouse(gl, 0);
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

    // move to controller ** later
    private void saveSimulation() {
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
            simModel.setTemperature(BigDecimal.valueOf(simulation.getTemperature()));
            simModel.setWindSpeed(BigDecimal.valueOf(simulation.getWindSpeed()));
            simModel.setWindDirection(BigDecimal.valueOf(simulation.getWindDirection()));
            simModel.setSunAngle(BigDecimal.valueOf(simulation.getSunAngle()));

            simulationService.addSimulation(simModel);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }


}
