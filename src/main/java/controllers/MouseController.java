package controllers;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.awt.GLCanvas;
import models.Simulation;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.nio.FloatBuffer;
import java.util.Objects;

public class MouseController {
    private static MouseController instance;
    private final Simulation simulation;
    private MouseController(Simulation simulation) {
        this.simulation = Objects.requireNonNull(simulation);
    }

    public static MouseController getInstance(Simulation simulation) {
        if (instance == null) {
            instance = new MouseController(simulation);
        }
        return instance;
    }

    public void updateMousePosition(GL4 gl, float x, float y) {
        simulation.setMousePosition(new float[]{x, y});

        gl.glUseProgram(simulation.getComputeProgram());
        gl.glUniform2fv(gl.glGetUniformLocation(simulation.getComputeProgram(), "mousePosition"), 1, FloatBuffer.wrap(simulation.getMousePosition()));
    }

    public void setMoveTowardsMouse(GL4 gl, int value) {
        simulation.setMoveTowardsMouse(value);
        gl.glUseProgram(simulation.getComputeProgram());
        gl.glUniform1i(gl.glGetUniformLocation(simulation.getComputeProgram(), "moveTowardsMouse"), value);
    }

    public void setMoveAwayFromMouse(GL4 gl, int value) {
        simulation.setMoveAwayFromMouse(value);
        gl.glUseProgram(simulation.getComputeProgram());
        gl.glUniform1i(gl.glGetUniformLocation(simulation.getComputeProgram(), "moveAwayFromMouse"), value);
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
