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

public class Chair extends JPanel implements GLEventListener {

    private GLU glu;
    private GLUT glut;
    private float rotateX, rotateY;
    private int lastX, lastY;
    private float chairColorR = 0.55f;
    private float chairColorG = 0.27f;
    private float chairColorB = 0.07f;
    private boolean isDaytime = true;
    private boolean shiftDown = false;
    private float chairTranslateX = 0;
    private float chairTranslateZ = 0;
    private float floorLength = 2.5f;
    private float floorWidth = 2.5f;
    private String floorShape = "Rectangle";
    private boolean is3DView = true;
    private boolean shadingEnabled = false;
    private boolean scaleFitEnabled = false;

    public Chair() {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastX;
                int dy = e.getY() - lastY;

                if (is3DView) {
                    if (shiftDown) {
                        rotateX += dy;
                        rotateY += dx;
                    } else {
                        chairTranslateX += dx * 0.01f;
                        chairTranslateZ += dy * 0.01f;

                        chairTranslateX = Math.max(-2.0f, Math.min(2.0f, chairTranslateX));
                        chairTranslateZ = Math.max(-2.0f, Math.min(2.0f, chairTranslateZ));
                    }
                } else {
                    chairTranslateX += dx * 0.01f;
                    chairTranslateZ -= dy * 0.01f;

                    chairTranslateX = Math.max(-2.0f, Math.min(2.0f, chairTranslateX));
                    chairTranslateZ = Math.max(-2.0f, Math.min(2.0f, chairTranslateZ));
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

        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftDown = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftDown = false;
                }
            }
        });

        canvas.setFocusable(true);
        canvas.requestFocusInWindow();

        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);

        JPanel colorPanel = new JPanel();

        JTextField floorLengthField = new JTextField(5);
        JTextField floorWidthField = new JTextField(5);
        floorLengthField.setText(String.valueOf(floorLength));
        floorWidthField.setText(String.valueOf(floorWidth));
        JButton applyFloorSizeButton = new JButton("Apply Floor Size");

        applyFloorSizeButton.addActionListener(e -> {
            try {
                float newLength = Float.parseFloat(floorLengthField.getText());
                float newWidth = Float.parseFloat(floorWidthField.getText());
                if (newLength > 0 && newWidth > 0) {
                    floorLength = newLength;
                    floorWidth = newWidth;
                    canvas.repaint();
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter positive numbers!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input! Please enter numbers.");
            }
        });

        colorPanel.add(new JLabel("Floor Length:"));
        colorPanel.add(floorLengthField);
        colorPanel.add(new JLabel("Floor Width:"));
        colorPanel.add(floorWidthField);
        colorPanel.add(applyFloorSizeButton);

        String[] shapes = {"Rectangle", "L-Shape"};
        JComboBox<String> shapeComboBox = new JComboBox<>(shapes);
        shapeComboBox.addActionListener(e -> {
            floorShape = (String) shapeComboBox.getSelectedItem();
            canvas.repaint();
        });
        colorPanel.add(new JLabel("Floor Shape:"));
        colorPanel.add(shapeComboBox);

        JSlider redSlider = new JSlider(0, 255, (int) (chairColorR * 255));
        redSlider.addChangeListener(e -> {
            chairColorR = redSlider.getValue() / 255f;
            canvas.repaint();
        });
        colorPanel.add(new JLabel("R:"));
        colorPanel.add(redSlider);

        JSlider greenSlider = new JSlider(0, 255, (int) (chairColorG * 255));
        greenSlider.addChangeListener(e -> {
            chairColorG = greenSlider.getValue() / 255f;
            canvas.repaint();
        });
        colorPanel.add(new JLabel("G:"));
        colorPanel.add(greenSlider);

        JSlider blueSlider = new JSlider(0, 255, (int) (chairColorB * 255));
        blueSlider.addChangeListener(e -> {
            chairColorB = blueSlider.getValue() / 255f;
            canvas.repaint();
        });
        colorPanel.add(new JLabel("B:"));
        colorPanel.add(blueSlider);

        JCheckBox shadingToggle = new JCheckBox("Enable Shading");
        shadingToggle.addActionListener(e -> {
            shadingEnabled = shadingToggle.isSelected();
            canvas.repaint();
        });
        colorPanel.add(shadingToggle);

        JCheckBox scaleFitToggle = new JCheckBox("Scale Fit");
        scaleFitToggle.addActionListener(e -> {
            scaleFitEnabled = scaleFitToggle.isSelected();
            canvas.repaint();
        });
        colorPanel.add(scaleFitToggle);

        JButton backgroundButton = new JButton("Day/Night");
        backgroundButton.addActionListener(e -> {
            isDaytime = !isDaytime;
            canvas.repaint();
        });
        colorPanel.add(backgroundButton);

        JButton saveButton = new JButton("Save PNG");
        saveButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();

                BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                canvas.paint(graphics);
                graphics.dispose();

                try {
                    ImageIO.write(image, "PNG", new File(filePath + ".png"));
                    JOptionPane.showMessageDialog(null, "Design saved successfully!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error saving design: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        colorPanel.add(saveButton);

        JButton deleteButton = new JButton("Delete Design");
        deleteButton.addActionListener(e -> {
            rotateX = 0;
            rotateY = 0;
            isDaytime = true;
            chairTranslateX = 0;
            chairTranslateZ = 0;
            canvas.repaint();
        });
        colorPanel.add(deleteButton);

        JButton toggleViewButton = new JButton("Toggle 2D/3D");
        toggleViewButton.addActionListener(e -> {
            is3DView = !is3DView;
            canvas.repaint();
        });
        colorPanel.add(toggleViewButton);

        add(colorPanel, BorderLayout.NORTH);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        glut = new GLUT();
        gl.glClearColor(0.96f, 0.96f, 0.86f, 1.0f);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        float[] lightPos = {0.0f, 5.0f, 0.0f, 1.0f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        gl.glTranslatef(0, 0, -5);
        if (is3DView) {
            gl.glRotatef(rotateX, 1, 0, 0);
            gl.glRotatef(rotateY, 0, 1, 0);
        }

        float[] matAmbient = {0.7f, 0.7f, 0.7f, 1.0f};
        float[] matDiffuse = {0.7f, 0.7f, 0.7f, 1.0f};
        float[] matSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] matShininess = {100.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, matAmbient, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, matDiffuse, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpecular, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, matShininess, 0);

        drawChair(gl, chairColorR, chairColorG, chairColorB);

        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        double aspect = (double) width / height;

        if (is3DView) {
            glu.gluPerspective(45.0, aspect, 0.1, 100.0);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
            glu.gluLookAt(0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
        } else {
            gl.glOrtho(-5, 5, -5 * height / (float) width, 5 * height / (float) width, -1, 1);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
        }
    }

    private void drawChair(GL2 gl, float r, float g, float b) {
        float lowestY = -1.5f;

        gl.glPushMatrix();
        gl.glTranslatef(chairTranslateX, 0.0f, chairTranslateZ);

        float scaleFactor = 1.0f;
        if (scaleFitEnabled) {
            scaleFactor = Math.min(floorLength, floorWidth) / 3.0f;
        }
        gl.glScalef(scaleFactor, scaleFactor, scaleFactor);

        if (shadingEnabled) {
            float[] woodAmbient = {r * 0.8f, g * 0.8f, b * 0.8f, 1.0f};
            float[] woodDiffuse = {r, g, b, 1.0f};
            gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, woodAmbient, 0);
            gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, woodDiffuse, 0);
        } else {
            gl.glDisable(GL2.GL_LIGHTING);
            gl.glColor3f(r, g, b);
        }

        for (float x = -0.4f; x <= 0.4f; x += 0.8f) {
            for (float z = -0.4f; z <= 0.4f; z += 0.8f) {
                gl.glPushMatrix();
                gl.glTranslatef(x, -1.0f, z);
                gl.glScalef(0.1f, 1.0f, 0.1f);
                glut.glutSolidCube(1);
                gl.glPopMatrix();
            }
        }

        gl.glPushMatrix();
        gl.glTranslatef(0.0f, -0.5f, 0.0f);
        gl.glScalef(0.8f, 0.1f, 0.8f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(0.0f, -0.2f, -0.4f);
        gl.glScalef(0.8f, 0.6f, 0.1f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        gl.glPopMatrix();

        if (!shadingEnabled) gl.glEnable(GL2.GL_LIGHTING);

        gl.glColor3f(1.0f, 1.0f, 1.0f);
        if (floorShape.equals("Rectangle")) {
            gl.glPushMatrix();
            gl.glTranslatef(0.0f, lowestY, 0.0f);
            gl.glScalef(floorLength, 0.1f, floorWidth);
            glut.glutSolidCube(1);
            gl.glPopMatrix();
        } else if (floorShape.equals("L-Shape")) {
            gl.glPushMatrix();
            gl.glTranslatef(-floorLength / 4, lowestY, 0.0f);
            gl.glScalef(floorLength / 2, 0.1f, floorWidth);
            glut.glutSolidCube(1);
            gl.glPopMatrix();

            gl.glPushMatrix();
            gl.glTranslatef(floorLength / 4, lowestY, -floorWidth / 4);
            gl.glScalef(floorLength / 2, 0.1f, floorWidth / 2);
            glut.glutSolidCube(1);
            gl.glPopMatrix();
        }

        if (isDaytime) {
            gl.glClearColor(0.96f, 0.96f, 0.86f, 1.0f);
        } else {
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chair Design");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 1000);
        frame.setLocationRelativeTo(null);

        Chair chair = new Chair();
        frame.add(chair);

        frame.setVisible(true);
    }
}
