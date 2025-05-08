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

public class DoubleBed extends JPanel implements GLEventListener {

    private GLU glu;
    private GLUT glut;
    private float rotateX, rotateY;
    private int lastX, lastY;
    private float[] bedColor = {0.54f, 0.27f, 0.07f};  // Default bed color (RGB)
    private boolean isDaytime = true;
    private boolean isSmoothShading = true;
    private float roomWidth = 2.5f;
    private float roomLength = 4.0f;
    private float bedHeightOffset = -1.5f;
    private float zoomLevel = -5.0f;
    private String roomShape = "Rectangle";

    private float bedPosX = 0.0f;
    private float bedPosZ = 0.0f;
    private boolean isDragging = false;
    private int dragStartX, dragStartY;
    private float originalBedPosX, originalBedPosZ;

    private boolean scaleToFitEnabled = false;
    private GLCanvas canvas;

    public DoubleBed() {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (!isDragging) {
                    int dx = e.getX() - lastX;
                    int dy = e.getY() - lastY;
                    rotateX += dy;
                    rotateY += dx;
                    lastX = e.getX();
                    lastY = e.getY();
                    canvas.repaint();
                }
            }

            public void mouseMoved(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
            }
        });

        canvas.addMouseWheelListener(e -> {
            zoomLevel += e.getWheelRotation() * 0.1f;
            canvas.repaint();
        });

        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    isDragging = true;
                    dragStartX = e.getX();
                    dragStartY = e.getY();
                    originalBedPosX = bedPosX;
                    originalBedPosZ = bedPosZ;
                }
            }

            public void mouseReleased(MouseEvent e) {
                isDragging = false;
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (isDragging && SwingUtilities.isLeftMouseButton(e)) {
                    float dx = e.getX() - dragStartX;
                    float dz = e.getY() - dragStartY;
                    bedPosX = originalBedPosX + dx * 0.01f;
                    bedPosZ = originalBedPosZ + dz * 0.01f;
                    canvas.repaint();
                }
            }
        });

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(3, 4, 10, 10)); // Add grid layout with an extra row for RGB sliders

        // Row 1
        JTextField widthField = new JTextField(String.valueOf(roomWidth), 5);
        JTextField lengthField = new JTextField(String.valueOf(roomLength), 5);
        JButton applyFloor = new JButton("Apply Floor Size");

        applyFloor.addActionListener(e -> {
            try {
                roomWidth = Float.parseFloat(widthField.getText());
                roomLength = Float.parseFloat(lengthField.getText());
                canvas.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid floor size input.");
            }
        });

        String[] shapes = {"Rectangle", "L-shape"};
        JComboBox<String> shapeComboBox = new JComboBox<>(shapes);
        shapeComboBox.addActionListener(e -> {
            roomShape = (String) shapeComboBox.getSelectedItem();
            canvas.repaint();
        });

        controlPanel.add(new JLabel("Room Width:"));
        controlPanel.add(widthField);
        controlPanel.add(new JLabel("Room Length:"));
        controlPanel.add(lengthField);
        controlPanel.add(applyFloor);
        controlPanel.add(new JLabel("Room Shape:"));
        controlPanel.add(shapeComboBox);

        // Row 2
        JCheckBox scaleToFitCheck = new JCheckBox("Scale to Fit");
        scaleToFitCheck.addItemListener(e -> {
            scaleToFitEnabled = scaleToFitCheck.isSelected();
            canvas.repaint();
        });

        JButton backgroundButton = new JButton("Day/Night");
        backgroundButton.addActionListener(e -> {
            isDaytime = !isDaytime;
            canvas.repaint();
        });

        JButton shadingToggle = new JButton("Toggle Shading");
        shadingToggle.addActionListener(e -> {
            isSmoothShading = !isSmoothShading;
            canvas.repaint();
        });

        JButton saveButton = new JButton("Save PNG");
        saveButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                canvas.paint(graphics);
                graphics.dispose();
                try {
                    ImageIO.write(image, "PNG", new File(selectedFile.getAbsolutePath() + ".png"));
                    JOptionPane.showMessageDialog(null, "Design saved successfully!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error saving design: " + ex.getMessage());
                }
            }
        });

        JButton deleteButton = new JButton("Delete Design");
        deleteButton.addActionListener(e -> {
            rotateX = 0;
            rotateY = 0;
            isDaytime = true;
            bedColor[0] = 0.54f;
            bedColor[1] = 0.27f;
            bedColor[2] = 0.07f;
            bedPosX = 0;
            bedPosZ = 0;
            canvas.repaint();
        });

        // Bed height slider setup
        JSlider heightSlider = new JSlider(-3, 0, -3); // Set lowest value as default (-3)
        heightSlider.addChangeListener(e -> {
            bedHeightOffset = heightSlider.getValue();
            canvas.repaint();
        });

        // Row 3 - RGB Sliders for bed color
        JSlider redSlider = new JSlider(0, 255, (int) (bedColor[0] * 255));
        JSlider greenSlider = new JSlider(0, 255, (int) (bedColor[1] * 255));
        JSlider blueSlider = new JSlider(0, 255, (int) (bedColor[2] * 255));

        redSlider.addChangeListener(e -> {
            bedColor[0] = redSlider.getValue() / 255.0f;
            canvas.repaint();
        });
        greenSlider.addChangeListener(e -> {
            bedColor[1] = greenSlider.getValue() / 255.0f;
            canvas.repaint();
        });
        blueSlider.addChangeListener(e -> {
            bedColor[2] = blueSlider.getValue() / 255.0f;
            canvas.repaint();
        });

        controlPanel.add(new JLabel("Bed Color:"));
        controlPanel.add(new JLabel("R"));
        controlPanel.add(new JLabel("G"));
        controlPanel.add(new JLabel("B"));
        controlPanel.add(redSlider);
        controlPanel.add(greenSlider);
        controlPanel.add(blueSlider);

        // Control panel 2nd row
        controlPanel.add(scaleToFitCheck);
        controlPanel.add(backgroundButton);
        controlPanel.add(shadingToggle);
        controlPanel.add(saveButton);
        controlPanel.add(new JLabel("Bed Height:"));
        controlPanel.add(heightSlider);
        controlPanel.add(deleteButton); // Added delete button here

        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.NORTH);
        setPreferredSize(new Dimension(1200, 1000));
        canvas.requestFocusInWindow();
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
        float[] lightPos = {0f, 5f, 0f, 1f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glShadeModel(isSmoothShading ? GL2.GL_SMOOTH : GL2.GL_FLAT);

        if (isDaytime) {
            gl.glClearColor(0.96f, 0.96f, 0.86f, 1.0f);
        } else {
            gl.glClearColor(0.0f, 0.0f, 0.1f, 1.0f);
        }

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, zoomLevel);
        gl.glRotatef(rotateX, 1, 0, 0);
        gl.glRotatef(rotateY, 0, 1, 0);

        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, bedColor, 0);
        drawBed(gl);
        drawFloor(gl);

        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        if (height <= 0) height = 1;
        float aspect = (float) width / height;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0, aspect, 1.0, 100.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    private void drawFloor(GL2 gl) {
        gl.glColor3f(1f, 1f, 1f);
        if ("Rectangle".equals(roomShape)) {
            gl.glPushMatrix();
            gl.glTranslatef(0.0f, -3.0f, 0.0f);
            gl.glScalef(roomWidth, 0.1f, roomLength);
            glut.glutSolidCube(1);
            gl.glPopMatrix();
        } else if ("L-shape".equals(roomShape)) {
            gl.glPushMatrix();
            gl.glTranslatef(-roomWidth / 4, -3.0f, 0.0f);
            gl.glScalef(roomWidth / 2, 0.1f, roomLength);
            glut.glutSolidCube(1);
            gl.glPopMatrix();

            gl.glPushMatrix();
            gl.glTranslatef(roomWidth / 4, -3.0f, -roomLength / 4);
            gl.glScalef(roomWidth / 2, 0.1f, roomLength / 2);
            glut.glutSolidCube(1);
            gl.glPopMatrix();
        }
    }

    private void drawBed(GL2 gl) {
        float y = bedHeightOffset;
        float scale = scaleToFitEnabled ? Math.min(roomWidth / 2f, roomLength / 2f) : 1.0f;

        gl.glPushMatrix();
        gl.glTranslatef(bedPosX, 0f, bedPosZ);
        gl.glScalef(scale, scale, scale);

        gl.glPushMatrix();
        gl.glTranslatef(0.0f, y + 0.2f, 0.0f);
        gl.glScalef(1.6f, 0.1f, 1.6f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(0.0f, y + 0.5f, -0.8f);
        gl.glScalef(1.6f, 0.6f, 0.1f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(-0.8f, y + 0.3f, 0.0f);
        gl.glScalef(0.1f, 0.4f, 1.6f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(0.8f, y + 0.3f, 0.0f);
        gl.glScalef(0.1f, 0.4f, 1.6f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(0.0f, y + 0.3f, 0.8f);
        gl.glScalef(1.6f, 0.4f, 0.1f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        gl.glPopMatrix();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("DoubleBed");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new DoubleBed());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.setResizable(true);
        });
    }
}
