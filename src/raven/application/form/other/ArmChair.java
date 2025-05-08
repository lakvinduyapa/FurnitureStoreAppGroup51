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

public class ArmChair extends JPanel implements GLEventListener {

    private GLU glu;
    private GLUT glut;
    private float rotateX, rotateY;
    private int lastX, lastY;
    private float chairColorR = 0.55f, chairColorG = 0.27f, chairColorB = 0.07f;
    private boolean isDaytime = true;
    private float roomLength = 2.5f, roomBreadth = 2.5f;
    private final float roomHeight = 2.5f;
    private String roomShape = "Rectangle";
    private float chairPosX = 0.0f, chairPosZ = 0.0f;
    private boolean is3DView = true;
    private boolean shadingEnabled = false;
    private boolean scaleFitEnabled = false;

    public ArmChair() {
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
                    if ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
                        rotateX += dy;
                        rotateY += dx;
                    } else if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
                        chairPosX += dx * 0.01f;
                        chairPosZ += dy * 0.01f;
                    }
                } else {
                    chairPosX += dx * 0.01f;
                    chairPosZ -= dy * 0.01f;
                }

                float maxX = (roomLength / 2f) - 0.5f;
                float maxZ = (roomBreadth / 2f) - 0.5f;

                chairPosX = Math.max(-maxX, Math.min(maxX, chairPosX));
                chairPosZ = Math.max(-maxZ, Math.min(maxZ, chairPosZ));

                if (roomShape.equals("L-Shape") && chairPosX > 0 && chairPosZ > 0) {
                    if (chairPosX > chairPosZ) {
                        chairPosX = chairPosZ;
                    } else {
                        chairPosZ = chairPosX;
                    }
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

        JPanel topPanel = new JPanel();

        JTextField lengthField = new JTextField(Float.toString(roomLength), 4);
        JTextField breadthField = new JTextField(Float.toString(roomBreadth), 4);
        topPanel.add(new JLabel("Length:")); topPanel.add(lengthField);
        topPanel.add(new JLabel("Breadth:")); topPanel.add(breadthField);

        JButton applySize = new JButton("Apply Room Size");
        applySize.addActionListener(e -> {
            try {
                roomLength = Float.parseFloat(lengthField.getText());
                roomBreadth = Float.parseFloat(breadthField.getText());
                canvas.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Enter valid float values for room dimensions.");
            }
        });
        topPanel.add(applySize);

        String[] shapes = {"Rectangle", "L-Shape"};
        JComboBox<String> shapeBox = new JComboBox<>(shapes);
        shapeBox.addActionListener(e -> {
            roomShape = (String) shapeBox.getSelectedItem();
            canvas.repaint();
        });
        topPanel.add(new JLabel("Room Shape:")); topPanel.add(shapeBox);

        JSlider redSlider = new JSlider(0, 255, (int) (chairColorR * 255));
        redSlider.addChangeListener(e -> {
            chairColorR = redSlider.getValue() / 255f;
            canvas.repaint();
        });
        topPanel.add(new JLabel("R:")); topPanel.add(redSlider);

        JSlider greenSlider = new JSlider(0, 255, (int) (chairColorG * 255));
        greenSlider.addChangeListener(e -> {
            chairColorG = greenSlider.getValue() / 255f;
            canvas.repaint();
        });
        topPanel.add(new JLabel("G:")); topPanel.add(greenSlider);

        JSlider blueSlider = new JSlider(0, 255, (int) (chairColorB * 255));
        blueSlider.addChangeListener(e -> {
            chairColorB = blueSlider.getValue() / 255f;
            canvas.repaint();
        });
        topPanel.add(new JLabel("B:")); topPanel.add(blueSlider);

        JCheckBox shadingToggle = new JCheckBox("Enable Shading");
        shadingToggle.addActionListener(e -> {
            shadingEnabled = shadingToggle.isSelected();
            canvas.repaint();
        });
        topPanel.add(shadingToggle);

        JCheckBox scaleToggle = new JCheckBox("Scale Fit");
        scaleToggle.addActionListener(e -> {
            scaleFitEnabled = scaleToggle.isSelected();
            canvas.repaint();
        });
        topPanel.add(scaleToggle);

        JButton backgroundButton = new JButton("Day/Night");
        backgroundButton.addActionListener(e -> {
            isDaytime = !isDaytime;
            canvas.repaint();
        });
        topPanel.add(backgroundButton);

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
        topPanel.add(saveButton);

        JButton deleteButton = new JButton("Delete Design");
        deleteButton.addActionListener(e -> {
            rotateX = 0;
            rotateY = 0;
            isDaytime = true;
            canvas.repaint();
        });
        topPanel.add(deleteButton);

        JButton toggleViewButton = new JButton("Toggle 2D/3D");
        toggleViewButton.addActionListener(e -> {
            is3DView = !is3DView;
            canvas.repaint();
        });
        topPanel.add(toggleViewButton);

        add(topPanel, BorderLayout.NORTH);
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

        drawChair(gl, chairColorR, chairColorG, chairColorB);
        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        if (is3DView) {
            double aspect = (double) width / height;
            glu.gluPerspective(45.0, aspect, 0.1, 100.0);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
            glu.gluLookAt(0.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
        } else {
            float orthoSize = 5f;
            gl.glOrtho(-orthoSize, orthoSize, -orthoSize * height / (float) width, orthoSize * height / (float) width, -1, 1);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
        }
    }

    private void drawChair(GL2 gl, float r, float g, float b) {
        float floorY = -roomHeight / 2f;

        gl.glPushMatrix();
        gl.glTranslatef(chairPosX, 0.0f, chairPosZ);

        float scale = scaleFitEnabled ? Math.min(roomLength, roomBreadth) / 3f : 1.0f;
        gl.glScalef(scale, scale, scale);

        if (shadingEnabled) {
            float[] ambient = {r * 0.8f, g * 0.8f, b * 0.8f, 1.0f};
            float[] diffuse = {r, g, b, 1.0f};
            gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, ambient, 0);
            gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffuse, 0);
        } else {
            gl.glDisable(GL2.GL_LIGHTING);
            gl.glColor3f(r, g, b);
        }

        for (float x = -0.4f; x <= 0.4f; x += 0.8f) {
            for (float z = -0.4f; z <= 0.4f; z += 0.8f) {
                gl.glPushMatrix();
                gl.glTranslatef(x, floorY + 0.5f, z);
                gl.glScalef(0.1f, 1.0f, 0.1f);
                glut.glutSolidCube(1);
                gl.glPopMatrix();
            }
        }

        gl.glPushMatrix();
        gl.glTranslatef(0.0f, floorY + 0.8f, 0.0f);
        gl.glScalef(0.8f, 0.1f, 0.8f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(0.0f, floorY + 1.3f, -0.4f);
        gl.glScalef(0.8f, 1f, 0.1f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(0.4f, floorY + 1.0f, 0.0f);
        gl.glScalef(0.1f, 0.5f, 0.9f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(-0.4f, floorY + 1.0f, 0.0f);
        gl.glScalef(0.1f, 0.5f, 0.9f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        gl.glPopMatrix();
        if (!shadingEnabled) gl.glEnable(GL2.GL_LIGHTING);

        if (roomShape.equals("Rectangle")) {
            gl.glPushMatrix();
            gl.glTranslatef(0.0f, floorY, 0.0f);
            gl.glScalef(roomLength, 0.1f, roomBreadth);
            glut.glutSolidCube(1);
            gl.glPopMatrix();
        } else if (roomShape.equals("L-Shape")) {
            gl.glPushMatrix();
            gl.glTranslatef(-roomLength / 4, floorY, 0.0f);
            gl.glScalef(roomLength / 2, 0.1f, roomBreadth);
            glut.glutSolidCube(1);
            gl.glPopMatrix();

            gl.glPushMatrix();
            gl.glTranslatef(roomLength / 4, floorY, -roomBreadth / 4);
            gl.glScalef(roomLength / 2, 0.1f, roomBreadth / 2);
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
        JFrame frame = new JFrame("Arm Chair");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 1000);
        frame.setLocationRelativeTo(null);
        frame.add(new ArmChair());
        frame.setVisible(true);
    }
}
