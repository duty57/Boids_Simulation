package views;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLCanvas;

import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import lombok.Getter;
import lombok.Setter;
import models.Simulation;


import javax.swing.*;
import java.awt.*;

@Getter
@Setter
public class MainFrame extends JFrame {

    private GLCanvas canvas;
    private Simulation simulation;

    public MainFrame() {
        super("Boid Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Initialize OpenGL
        GLProfile profile = GLProfile.get(GLProfile.GL4);
        GLCapabilities capabilities = new GLCapabilities(profile);
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
    }

private JPanel createSettingsPanel() {
    JPanel mainPanel = new JPanel();
    mainPanel.setPreferredSize(new Dimension(200, 600));
    mainPanel.setLayout(new BorderLayout());

    JTabbedPane tabbedPane = new JTabbedPane();

    // Simulation Tab
    JPanel simulationPanel = new JPanel();
    simulationPanel.setLayout(new BoxLayout(simulationPanel, BoxLayout.Y_AXIS));

    // Add sliders for simulation parameters
    simulationPanel.add(createSliderPanel("Max Speed", 0, 100, (int) (simulation.getMaxSpeed() * 100)));
    simulationPanel.add(createSliderPanel("Alignment Force", 0, 10, (int) (simulation.getAlignmentForce() * 100)));
    simulationPanel.add(createSliderPanel("Cohesion Force", 0, 10, (int)(simulation.getCohesionForce() * 100)));
    simulationPanel.add(createSliderPanel("Separation Force", 0, 10, (int)(simulation.getSeparationForce() * 100)));
    simulationPanel.add(createSliderPanel("Vision Range", 0, 10, (int)(simulation.getVision() * 100)));
    simulationPanel.add(createSliderPanel("Drag Force", 0, 100, (int) (simulation.getDragForce() * 100)));
    simulationPanel.add(createSliderPanel("Temperature", -50, 50, (int) (simulation.getTemperature())));
    simulationPanel.add(createSliderPanel("Wind Speed", 0, 100, (int)(simulation.getWindSpeed() * 100)));
    simulationPanel.add(createSliderPanel("Cloudiness", 0, 100, (int)(simulation.getCloudiness() * 100)));
    simulationPanel.add(createSliderPanel("Sun Position", 0, 180, (int)(simulation.getSunAngle())));

    JScrollPane simulationScroll = new JScrollPane(simulationPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


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

    mainPanel.add(tabbedPane, BorderLayout.CENTER);
    return mainPanel;
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
            float normalizedValue = value / (label.equals("Temperature") ? 1.0f : 100.0f);
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

    private JPanel createCheckboxPanel(String label, boolean initialState) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JCheckBox checkBox = new JCheckBox(label, initialState);
        checkBox.addActionListener(e -> {
            // Here you would add code to update the simulation parameters
        });

        panel.add(checkBox);
        return panel;
    }


}
