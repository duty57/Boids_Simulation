package views;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import lombok.Getter;


import javax.swing.*;
import java.awt.*;

@Getter
public class MainFrame extends JFrame {

    private GLCanvas canvas;

    public MainFrame() {
        super("Boid Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Initialize OpenGL
        GLProfile profile = GLProfile.get(GLProfile.GL4);
        GLCapabilities capabilities = new GLCapabilities(profile);
//        init(capabilities);

    }


    // In MainFrame.java - modify the init method
    public void init(GLCapabilities capabilities) {
        // Create canvas
        canvas = new GLCanvas(capabilities);
        canvas.setPreferredSize(new Dimension(800, 600));

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
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(200, 600));
        panel.setBorder(BorderFactory.createTitledBorder("Simulation Settings"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Add sliders for various simulation parameters
        panel.add(createSliderPanel("Max Speed", 0, 100, 50));
        panel.add(createSliderPanel("Alignment Force", 0, 100, 50));
        panel.add(createSliderPanel("Cohesion Force", 0, 100, 50));
        panel.add(createSliderPanel("Separation Force", 0, 100, 50));
        panel.add(createSliderPanel("Vision Range", 0, 100, 50));
        panel.add(createSliderPanel("Drag Force", 0, 100, 10));

        // Add checkboxes for mouse behaviors
        panel.add(createCheckboxPanel("Attract to Mouse", false));
        panel.add(createCheckboxPanel("Repel from Mouse", false));

        return panel;
    }

    private JPanel createSliderPanel(String label, int min, int max, int initial) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel titleLabel = new JLabel(label);
        JSlider slider = new JSlider(min, max, initial);
        JLabel valueLabel = new JLabel(String.valueOf(initial));

        slider.addChangeListener(e -> {
            int value = slider.getValue();
            valueLabel.setText(String.valueOf(value));
            // Here you would add code to update the simulation parameters
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
