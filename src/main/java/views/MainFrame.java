package views;

import clients.WeatherAPIClient;
import com.jogamp.opengl.GL4;
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
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

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
            WeatherAPIClient.CityData cityData = simulationController.getCityParameters(cityName.getText());
            if (cityData != null) {
                simulation.getCityData(cityData);
                searchStatusLabel.setText("Data imported successfully");
                searchStatusLabel.setForeground(Color.GREEN);
            }else{
                searchStatusLabel.setText("City not found");
                searchStatusLabel.setForeground(Color.RED);
            }
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
                protected SimModel doInBackground() throws Exception {
                    return simulationController.saveSimulation(historyListModel);
                }

                @Override
                protected void done() {
                }
            }.execute();
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
            simulationController.updateSliderValues(slider.getValue(), valueLabel.getText());
        });

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.EAST);

        return panel;
    }

    //move to controller
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
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    simulationController.setMoveAwayFromMouse(gl, 1);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("mouseReleased");
                GL4 gl = canvas.getGL().getGL4();
                if (e.getButton() == MouseEvent.BUTTON1) {
                    simulationController.setMoveTowardsMouse(gl, 0);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
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
}
