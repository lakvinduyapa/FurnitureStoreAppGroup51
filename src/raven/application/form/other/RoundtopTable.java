package raven.application.form.other;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class RoundtopTable extends JPanel implements GLEventListener {

    private GLU glu;
    private GLUT glut;
    private float rotateX, rotateY;
    private int lastX, lastY;
    private float[] tableColor = {0.54f, 0.27f, 0.07f};
    private float[] floorColor = {1.0f, 1.0f, 1.0f};
    private boolean isDaytime = true;
    private boolean is3D = true;
    private boolean shadingEnabled = true;
    private float tablePosX = 0, tablePosZ = 0;
    private boolean dragging = false;

    private float roomWidth = 2.5f;
    private float roomDepth = 2.5f;
    private String roomShape = "Rectangle"; // "Rectangle" or "L-Shape"

    public RoundtopTable() {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);

        // Mouse controls for rotate / drag
        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && dragging) {
                    tablePosX += (e.getX() - lastX) * 0.01f;
                    tablePosZ += (e.getY() - lastY) * 0.01f;
                } else {
                    rotateX += (e.getY() - lastY);
                    rotateY += (e.getX() - lastX);
                }
                lastX = e.getX();
                lastY = e.getY();
                canvas.repaint();
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
            }
        });
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragging = SwingUtilities.isLeftMouseButton(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }
        });

        // Build control panel - single line
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Table Color sliders
        controlPanel.add(new JLabel("Table Color:"));
        controlPanel.add(createColorSliderPanel("R", 0, tableColor, canvas));
        controlPanel.add(createColorSliderPanel("G", 1, tableColor, canvas));
        controlPanel.add(createColorSliderPanel("B", 2, tableColor, canvas));

        // Floor Color sliders
        controlPanel.add(new JLabel("Floor Color:"));
        controlPanel.add(createColorSliderPanel("R", 0, floorColor, canvas));
        controlPanel.add(createColorSliderPanel("G", 1, floorColor, canvas));
        controlPanel.add(createColorSliderPanel("B", 2, floorColor, canvas));

        // Room shape
        controlPanel.add(new JLabel("Room Shape:"));
        JComboBox<String> shapeBox = new JComboBox<>(new String[]{"Rectangle", "L-Shape"});
        shapeBox.addActionListener(e -> {
            roomShape = (String) shapeBox.getSelectedItem();
            canvas.repaint();
        });
        controlPanel.add(shapeBox);

        // Toggles
        JButton toggleViewButton = new JButton("2D/3D");
        toggleViewButton.addActionListener(e -> { is3D = !is3D; canvas.repaint(); });
        controlPanel.add(toggleViewButton);

        JButton dayNightButton = new JButton("Day/Night");
        dayNightButton.addActionListener(e -> { isDaytime = !isDaytime; canvas.repaint(); });
        controlPanel.add(dayNightButton);

        JButton shadingButton = new JButton("Shading");
        shadingButton.addActionListener(e -> { shadingEnabled = !shadingEnabled; canvas.repaint(); });
        controlPanel.add(shadingButton);

        // Room size controls
        controlPanel.add(new JLabel("W:"));
        JTextField widthField = new JTextField(String.valueOf(roomWidth), 4);
        controlPanel.add(widthField);
        controlPanel.add(new JLabel("D:"));
        JTextField depthField = new JTextField(String.valueOf(roomDepth), 4);
        controlPanel.add(depthField);
        JButton applySize = new JButton("Apply Room Size");
        applySize.addActionListener(e -> {
            try {
                roomWidth = Float.parseFloat(widthField.getText());
                roomDepth = Float.parseFloat(depthField.getText());
                canvas.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid room size values.");
            }
        });
        controlPanel.add(applySize);

        // Save/Delete buttons
        JButton saveButton = new JButton("Save PNG");
        saveButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                canvas.paint(g);
                g.dispose();
                try {
                    ImageIO.write(image, "PNG", new File(file.getAbsolutePath() + ".png"));
                    JOptionPane.showMessageDialog(null, "Saved successfully.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Failed to save: " + ex.getMessage());
                }
            }
        });
        controlPanel.add(saveButton);

        JButton deleteButton = new JButton("Delete Design");
        deleteButton.addActionListener(e -> {
            rotateX = rotateY = 0;
            tablePosX = tablePosZ = 0;
            isDaytime = true;
            canvas.repaint();
        });
        controlPanel.add(deleteButton);

        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.NORTH);
    }

    private JPanel createColorSliderPanel(String label, int index, float[] colorArray, GLCanvas canvas) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        JSlider slider = new JSlider(0, 255, (int)(colorArray[index] * 255));
        slider.setPreferredSize(new Dimension(100, 20));
        slider.addChangeListener((ChangeEvent e) -> {
            colorArray[index] = slider.getValue() / 255f;
            canvas.repaint();
        });
        panel.add(new JLabel(label + ":"));
        panel.add(slider);
        return panel;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        glut = new GLUT();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_LIGHTING);
        rotateX = rotateY = 0;
    }

    @Override public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        // Background: Day or Night
        if (isDaytime) gl.glClearColor(0.96f, 0.96f, 0.86f, 1.0f);
        else           gl.glClearColor(0.0f, 0.0f, 0.1f, 1.0f);

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        // Camera / projection
        if (is3D) {
            gl.glTranslatef(0, 0, -5);
            gl.glRotatef(rotateX, 1, 0, 0);
            gl.glRotatef(rotateY, 0, 1, 0);
        } else {
            gl.glOrtho(-5, 5, -5, 5, -5, 5);
        }

        // Toggle lighting on/off for flat or shaded
        if (shadingEnabled) gl.glEnable(GL2.GL_LIGHTING);
        else                gl.glDisable(GL2.GL_LIGHTING);

        drawTable(gl);
        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0, (double)width/height, 0.1, 100.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    private void drawTable(GL2 gl) {
        // --- Tabletop ---
        gl.glPushMatrix();
        gl.glTranslatef(tablePosX, -0.45f, tablePosZ);
        gl.glScalef(2.0f, 0.05f, 1.1f);
        if (shadingEnabled) {
            float[] amb = {tableColor[0]*0.3f, tableColor[1]*0.3f, tableColor[2]*0.3f, 1.0f};
            float[] diff = {tableColor[0], tableColor[1], tableColor[2], 1.0f};
            gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, amb, 0);
            gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diff, 0);
        } else {
            gl.glColor3fv(tableColor, 0);
        }
        glut.glutSolidSphere(0.5, 30, 30);
        gl.glPopMatrix();

        // --- Table Legs ---
        for (int i = 0; i < 4; i++) {
            double angle = i * Math.PI/2;
            double x = tablePosX + Math.cos(angle)*0.4;
            double z = tablePosZ + Math.sin(angle)*0.4;
            gl.glPushMatrix();
            gl.glTranslatef((float)x, -0.9f, (float)z);
            gl.glScalef(0.1f, 0.9f, 0.1f);
            if (!shadingEnabled) gl.glColor3fv(tableColor, 0);
            glut.glutSolidCube(1);
            gl.glPopMatrix();
        }

        // --- Floor (Rectangle or L-Shape) ---
        if (roomShape.equals("Rectangle")) {
            gl.glPushMatrix();
            gl.glTranslatef(0.0f, -1.33f, 0.0f);
            gl.glScalef(roomWidth, 0.1f, roomDepth);
            if (shadingEnabled) {
                float[] ambF = {floorColor[0]*0.3f, floorColor[1]*0.3f, floorColor[2]*0.3f, 1.0f};
                float[] diffF = {floorColor[0], floorColor[1], floorColor[2], 1.0f};
                gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, ambF, 0);
                gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffF, 0);
            } else {
                gl.glColor3fv(floorColor, 0);
            }
            glut.glutSolidCube(1);
            gl.glPopMatrix();
        } else {
            // L-Shape: two overlapping rectangles
            // Back half
            gl.glPushMatrix();
            gl.glTranslatef(0.0f, -1.33f, -roomDepth/4f);
            gl.glScalef(roomWidth, 0.1f, roomDepth/2f);
            applyFloorColorOrMaterial(gl);
            glut.glutSolidCube(1);
            gl.glPopMatrix();
            // Right half
            gl.glPushMatrix();
            gl.glTranslatef(roomWidth/4f, -1.33f, 0.0f);
            gl.glScalef(roomWidth/2f, 0.1f, roomDepth);
            applyFloorColorOrMaterial(gl);
            glut.glutSolidCube(1);
            gl.glPopMatrix();
        }
    }

    // helper for L-shape floor pieces
    private void applyFloorColorOrMaterial(GL2 gl) {
        if (shadingEnabled) {
            float[] ambF = {floorColor[0]*0.3f, floorColor[1]*0.3f, floorColor[2]*0.3f, 1.0f};
            float[] diffF = {floorColor[0], floorColor[1], floorColor[2], 1.0f};
            gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, ambF, 0);
            gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffF, 0);
        } else {
            gl.glColor3fv(floorColor, 0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Round Top Table");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 1000);
            frame.setLocationRelativeTo(null);
            frame.add(new RoundtopTable());
            frame.setVisible(true);
        });
    }
}
