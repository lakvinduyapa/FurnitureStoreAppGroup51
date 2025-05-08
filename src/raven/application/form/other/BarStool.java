package raven.application.form.other;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BarStool extends JPanel implements GLEventListener {
    private GLU glu;
    private GLUT glut;
    private float rotateX, rotateY;
    private int lastX, lastY;

    private float stoolColorR = 0.55f, stoolColorG = 0.27f, stoolColorB = 0.07f;
    private float floorColorR = 1.0f, floorColorG = 1.0f, floorColorB = 1.0f;

    private boolean isDaytime = true;
    private float roomLength = 2.0f, roomWidth = 2.0f;

    private float barStoolX = 0.0f, barStoolZ = 0.0f;
    private boolean isDraggingStool = false;
    private boolean isShadingEnabled = true;
    private boolean isScaleToFit = false;
    private boolean is3D = true;

    public BarStool() {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
                if (SwingUtilities.isLeftMouseButton(e)) {
                    isDraggingStool = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDraggingStool = false;
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastX;
                int dy = e.getY() - lastY;

                if (isDraggingStool) {
                    barStoolX += dx * 0.01f;
                    barStoolZ -= dy * 0.01f;
                } else {
                    rotateX += dy;
                    rotateY += dx;
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

        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new GridLayout(7, 1));

        // Room Size
        JPanel roomPanel = new JPanel();
        JTextField lengthField = new JTextField("2.0", 5);
        JTextField widthField = new JTextField("2.0", 5);
        JButton applySize = new JButton("Apply Room Size");
        applySize.addActionListener(e -> {
            try {
                roomLength = Float.parseFloat(lengthField.getText());
                roomWidth = Float.parseFloat(widthField.getText());
                canvas.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid size values.");
            }
        });
        roomPanel.add(new JLabel("Length:"));
        roomPanel.add(lengthField);
        roomPanel.add(new JLabel("Width:"));
        roomPanel.add(widthField);
        roomPanel.add(applySize);

        // Stool Color
        JPanel stoolColorPanel = new JPanel();
        stoolColorPanel.add(new JLabel("Stool Color"));
        JSlider redS = new JSlider(0, 255, (int)(stoolColorR * 255));
        JSlider greenS = new JSlider(0, 255, (int)(stoolColorG * 255));
        JSlider blueS = new JSlider(0, 255, (int)(stoolColorB * 255));
        redS.addChangeListener(e -> { stoolColorR = redS.getValue()/255f; canvas.repaint(); });
        greenS.addChangeListener(e -> { stoolColorG = greenS.getValue()/255f; canvas.repaint(); });
        blueS.addChangeListener(e -> { stoolColorB = blueS.getValue()/255f; canvas.repaint(); });
        stoolColorPanel.add(new JLabel("R:")); stoolColorPanel.add(redS);
        stoolColorPanel.add(new JLabel("G:")); stoolColorPanel.add(greenS);
        stoolColorPanel.add(new JLabel("B:")); stoolColorPanel.add(blueS);

        // Floor Color
        JPanel floorColorPanel = new JPanel();
        floorColorPanel.add(new JLabel("Floor Color"));
        JSlider redF = new JSlider(0, 255, (int)(floorColorR * 255));
        JSlider greenF = new JSlider(0, 255, (int)(floorColorG * 255));
        JSlider blueF = new JSlider(0, 255, (int)(floorColorB * 255));
        redF.addChangeListener(e -> { floorColorR = redF.getValue()/255f; canvas.repaint(); });
        greenF.addChangeListener(e -> { floorColorG = greenF.getValue()/255f; canvas.repaint(); });
        blueF.addChangeListener(e -> { floorColorB = blueF.getValue()/255f; canvas.repaint(); });
        floorColorPanel.add(new JLabel("R:")); floorColorPanel.add(redF);
        floorColorPanel.add(new JLabel("G:")); floorColorPanel.add(greenF);
        floorColorPanel.add(new JLabel("B:")); floorColorPanel.add(blueF);

        // Control Buttons
        JPanel buttonPanel = new JPanel();
        JButton toggleDay = new JButton("Day/Night");
        toggleDay.addActionListener(e -> { isDaytime = !isDaytime; canvas.repaint(); });
        JButton save = new JButton("Save PNG");
        save.addActionListener(e -> saveImage(canvas));
        JButton reset = new JButton("Reset View");
        reset.addActionListener(e -> {
            rotateX = 0;
            rotateY = 0;
            barStoolX = 0;
            barStoolZ = 0;
            isDaytime = true;
            canvas.repaint();
        });
        JButton toggleShading = new JButton("Toggle Shading");
        toggleShading.addActionListener(e -> { isShadingEnabled = !isShadingEnabled; canvas.repaint(); });
        JCheckBox scaleToFitCheckBox = new JCheckBox("Scale to Fit");
        scaleToFitCheckBox.addActionListener(e -> {
            isScaleToFit = scaleToFitCheckBox.isSelected();
            canvas.repaint();
        });
        JButton toggleView = new JButton("Toggle 2D/3D");
        toggleView.addActionListener(e -> {
            is3D = !is3D;
            canvas.repaint();
        });

        buttonPanel.add(toggleDay);
        buttonPanel.add(save);
        buttonPanel.add(reset);
        buttonPanel.add(toggleShading);
        buttonPanel.add(scaleToFitCheckBox);
        buttonPanel.add(toggleView);

        controlPanel.add(roomPanel);
        controlPanel.add(stoolColorPanel);
        controlPanel.add(floorColorPanel);
        controlPanel.add(buttonPanel);
        add(controlPanel, BorderLayout.NORTH);
    }

    private void saveImage(GLCanvas canvas) {
        BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        canvas.paint(g2d);
        g2d.dispose();
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                ImageIO.write(image, "PNG", new File(fileChooser.getSelectedFile().getAbsolutePath() + ".png"));
                JOptionPane.showMessageDialog(null, "Saved!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        glut = new GLUT();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        float[] lightPos = {0.0f, 5.0f, 0.0f, 1.0f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        if (isDaytime)
            gl.glClearColor(0.96f, 0.96f, 0.86f, 1.0f);
        else
            gl.glClearColor(0f, 0f, 0f, 1f);

        gl.glTranslatef(0, 0, -5);
        gl.glRotatef(rotateX, 1, 0, 0);
        gl.glRotatef(rotateY, 0, 1, 0);

        if (isScaleToFit) {
            gl.glScalef(0.8f, 0.8f, 0.8f);
        }

        if (isShadingEnabled) {
            gl.glShadeModel(GL2.GL_SMOOTH);
        } else {
            gl.glShadeModel(GL2.GL_FLAT);
        }

        if (!is3D) {
            gl.glLoadIdentity();
            gl.glTranslatef(0, 0, -10);
            gl.glRotatef(90, 1, 0, 0);
        }

        drawBarstool(gl);
        gl.glFlush();
    }

    private void drawBarstool(GL2 gl) {
        float[] stoolAmbient = {stoolColorR * 0.8f, stoolColorG * 0.8f, stoolColorB * 0.8f, 1.0f};
        float[] stoolDiffuse = {stoolColorR, stoolColorG, stoolColorB, 1.0f};

        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, stoolAmbient, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, stoolDiffuse, 0);

        gl.glPushMatrix();
        gl.glTranslatef(barStoolX, 0.0f, barStoolZ);
        gl.glScalef(1f, 0.1f, 0.6f);
        glut.glutSolidSphere(0.3, 20, 20);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(barStoolX, 0.0f, barStoolZ);
        gl.glRotatef(90, 1, 0, 0);
        glut.glutSolidCylinder(0.05, 1, 20, 20);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(barStoolX, -0.85f, barStoolZ + 0.1f);
        gl.glRotatef(-45, 1, 0, 0);
        gl.glScalef(0.1f, 0.1f, 0.1f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(barStoolX, -0.9f, barStoolZ + 0.2f);
        gl.glScalef(0.6f, 0.05f, 0.1f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        float[] floorAmbient = {floorColorR, floorColorG, floorColorB, 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, floorAmbient, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, floorAmbient, 0);
        gl.glPushMatrix();
        gl.glTranslatef(0.0f, -1.0f, 0.0f);
        gl.glScalef(roomLength, 0.1f, roomWidth);
        glut.glutSolidCube(1);
        gl.glPopMatrix();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0f, (float) width / height, 0.1f, 100.0f);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bar Stool Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(new BarStool());
        frame.setVisible(true);
    }
}
