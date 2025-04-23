package views;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLCanvas;

import com.jogamp.opengl.util.FPSAnimator;
import controllers.SimulationController;
import lombok.Getter;
import lombok.Setter;
import models.SimModel;
import models.Simulation;
import models.SimulationCard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Getter
@Setter
public class MainFrame extends JFrame {

    private GLCanvas canvas;
    private Simulation simulation;
    private SimulationController simulationController;
    private SimulationCard selectedCard;
    DefaultListModel<SimulationCard> historyListModel;
    JPanel simulationPanel;

    public MainFrame(SimulationController simulationController, Simulation simulation) {
        super("Boid Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        this.simulationController = simulationController;
        this.simulation = simulation;
        historyListModel = new DefaultListModel<>();
        simulationController.loadSimulations(historyListModel);
    }


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
        simulationController.setupMouseListeners(canvas);
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

    private void importSimulationPane(JTabbedPane tabbedPane, JScrollPane simulationScroll) {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search components
        JLabel searchLabel = new JLabel("Enter city name:");
        searchLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField cityName = new JTextField();
        cityName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel searchStatusLabel = new JLabel(" ");
        searchStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel searchButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton searchButton = new JButton("Search");
        searchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchButtonPanel.add(searchButton);

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // History section
        JLabel historyLabel = new JLabel("Saved Simulations:");
        historyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JList<SimulationCard> historyList = new JList<>(historyListModel);
        historyList.setCellRenderer(new SimulationCardRenderer());
        historyList.setFixedCellHeight(80);

        JScrollPane historyScrollPane = new JScrollPane(historyList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        historyScrollPane.setPreferredSize(new Dimension(180, 375));
        historyScrollPane.setMaximumSize(new Dimension(180, 375));

        // Bottom buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setMaximumSize(new Dimension(180, 30));

        JButton importButton = new JButton("Import");
        JButton deleteButton = new JButton("Delete");
        deleteButton.setBackground(Color.getHSBColor(0.0f, 0.73f, 0.90f));

        historyList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedCard = SimulationCardRenderer.onMouseClicked(e, historyListModel, historyList, selectedCard);
            }
        });

        importButton.addActionListener(e -> {
            if (selectedCard != null) {
                System.out.println("Importing");
                simulation.update(simulationController.importSimulationData(selectedCard));
                simulationController.importSliderValues(simulationPanel, simulationController.importSimulationData(selectedCard));
            }
        });

        deleteButton.addActionListener(e -> {
            if (selectedCard != null) {
                historyListModel.removeElement(selectedCard);
                simulationController.deleteSimulation(selectedCard, historyListModel);
                selectedCard = null;
            }
        });

        searchButton.addActionListener(e -> {
            searchStatusLabel.setText("Loading...");
            searchStatusLabel.setForeground(Color.BLUE);
            simulationController.getCityParameters(cityName.getText(), cityData -> {
                if (cityData != null) {
                    simulation.getCityData(cityData);
                    searchStatusLabel.setText("Data imported successfully");
                    searchStatusLabel.setForeground(Color.GREEN);
                } else {
                    searchStatusLabel.setText("City not found");
                    searchStatusLabel.setForeground(Color.RED);
                }
            });
        });

        buttonPanel.add(importButton);
        buttonPanel.add(deleteButton);

        // Add all components
        searchPanel.add(searchLabel);
        searchPanel.add(Box.createVerticalStrut(5));
        searchPanel.add(cityName);
        searchPanel.add(Box.createVerticalStrut(5));
        searchPanel.add(searchStatusLabel);
        searchPanel.add(Box.createVerticalStrut(5));
        searchPanel.add(searchButtonPanel);
        searchPanel.add(separator);
        searchPanel.add(historyLabel);
        searchPanel.add(Box.createVerticalStrut(5));
        searchPanel.add(historyScrollPane);
        searchPanel.add(Box.createVerticalStrut(5));
        searchPanel.add(buttonPanel);
        searchPanel.add(Box.createVerticalStrut(10));
        searchPanel.add(Box.createVerticalGlue());

        tabbedPane.addTab("Simulation", simulationScroll);
        tabbedPane.addTab("Search", searchPanel);
    }

    private JScrollPane getSimulationPane() {
        // Simulation Tab
        simulationPanel = new JPanel();
        simulationPanel.setLayout(new BoxLayout(simulationPanel, BoxLayout.Y_AXIS));

        // Add sliders for simulation parameters
        simulationPanel.add(createSliderPanel("Max Speed", 1, 100, (int) (simulation.getMaxSpeed() * 100)));
        simulationPanel.add(createSliderPanel("Alignment Force", 0, 10, (int) (simulation.getAlignmentForce() * 100)));
        simulationPanel.add(createSliderPanel("Cohesion Force", 0, 10, (int) (simulation.getCohesionForce() * 100)));
        simulationPanel.add(createSliderPanel("Separation Force", 0, 10, (int) (simulation.getSeparationForce() * 100)));
        simulationPanel.add(createSliderPanel("Vision Range", 0, 10, (int) (simulation.getVision() * 100)));
        simulationPanel.add(createSliderPanel("Drag Force", 0, 100, (int) (simulation.getDragForce() * 100)));
        simulationPanel.add(createSliderPanel("Temperature", -50, 50, (int) (simulation.getTemperature())));
        simulationPanel.add(createSliderPanel("Wind Speed", 0, 100, (int) (simulation.getWindSpeed() * 100)));
        simulationPanel.add(createSliderPanel("Cloudiness", 0, 100, (int) (simulation.getCloudiness() * 100)));
        simulationPanel.add(createSliderPanel("Sun Position", 0, 180, (int) (simulation.getSunAngle())));
        //drag radius is missing
        JPanel simSavePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton simSaveButton = new JButton("Save");
        simSaveButton.setBackground(Color.getHSBColor(0.25f, 0.8f, 0.69f));
        simSavePanel.add(simSaveButton);
        simulationPanel.add(Box.createVerticalStrut(10));
        simulationPanel.add(simSavePanel);

        simSaveButton.addActionListener(e -> {
            new SwingWorker<SimModel, Void>() {
                @Override
                protected SimModel doInBackground() {
                    return simulationController.saveSimulation(historyListModel);
                }
            }.execute();
        });

        return new JScrollPane(simulationPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
                case "Temperature" -> simulation.setTemperature((value + 50.0f) / 100.0f);//remake
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

}
