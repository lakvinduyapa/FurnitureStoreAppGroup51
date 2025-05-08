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

public class SingleBed extends JPanel implements GLEventListener {

    private GLU glu;
    private GLUT glut;
    private float rotateX, rotateY;
    private int lastX, lastY;
    private float bedColorR = 0.55f;
    private float bedColorG = 0.27f;
    private float bedColorB = 0.07f;
    private boolean isDaytime = true;
    private boolean isDragging = false;
    private boolean isRotating = false;

    private float bedPositionX = 0.0f;
    private float bedPositionZ = 0.0f;
    private float bedPositionY = -1.2f;

    private float roomWidth = 2.5f;
    private float roomLength = 4.0f;
    private String roomShape = "Rectangular";
    private boolean is2DView = false;
    private boolean autoScaleToFit = false;
    private boolean applyShade = false;
    private float legHeight = 0.75f;

    public SingleBed() {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (isRotating) {
                    int dx = e.getX() - lastX;
                    int dy = e.getY() - lastY;
                    rotateX += dy;
                    rotateY += dx;
                    lastX = e.getX();
                    lastY = e.getY();
                    canvas.repaint();
                } else if (isDragging) {
                    int dx = e.getX() - lastX;
                    int dy = e.getY() - lastY;
                    bedPositionX += dx * 0.01f;
                    bedPositionZ -= dy * 0.01f;
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

        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    isRotating = true;
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    isDragging = true;
                }
                lastX = e.getX();
                lastY = e.getY();
            }
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
                isRotating = false;
            }
        });

        canvas.addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation();
            bedPositionY += rotation * 0.1f;
            if (bedPositionY < -2.0f) bedPositionY = -2.0f;
            canvas.repaint();
        });

        JPanel controlPanel = new JPanel();

        JSlider redSlider = new JSlider(0, 255, (int) (bedColorR * 255));
        redSlider.addChangeListener(e -> {
            bedColorR = redSlider.getValue() / 255f;
            canvas.repaint();
        });
        controlPanel.add(new JLabel("R:"));
        controlPanel.add(redSlider);

        JSlider greenSlider = new JSlider(0, 255, (int) (bedColorG * 255));
        greenSlider.addChangeListener(e -> {
            bedColorG = greenSlider.getValue() / 255f;
            canvas.repaint();
        });
        controlPanel.add(new JLabel("G:"));
        controlPanel.add(greenSlider);

        JSlider blueSlider = new JSlider(0, 255, (int) (bedColorB * 255));
        blueSlider.addChangeListener(e -> {
            bedColorB = blueSlider.getValue() / 255f;
            canvas.repaint();
        });
        controlPanel.add(new JLabel("B:"));
        controlPanel.add(blueSlider);

        JButton backgroundButton = new JButton("Toggle Day/Night");
        backgroundButton.addActionListener(e -> {
            isDaytime = !isDaytime;
            canvas.repaint();
        });
        controlPanel.add(backgroundButton);

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
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                }
            }
        });
        controlPanel.add(saveButton);

        JButton deleteButton = new JButton("Delete Design");
        deleteButton.addActionListener(e -> {
            rotateX = 0;
            rotateY = 0;
            bedPositionX = 0.0f;
            bedPositionZ = 0.0f;
            bedPositionY = -1.2f;
            isDaytime = true;
            canvas.repaint();
        });
        controlPanel.add(deleteButton);

        controlPanel.add(new JLabel("Room Size:"));
        JTextField widthField = new JTextField(Float.toString(roomWidth), 4);
        JTextField lengthField = new JTextField(Float.toString(roomLength), 4);
        controlPanel.add(new JLabel("Width:"));
        controlPanel.add(widthField);
        controlPanel.add(new JLabel("Length:"));
        controlPanel.add(lengthField);
        JButton applySizeButton = new JButton("Apply Size");
        applySizeButton.addActionListener(e -> {
            try {
                roomWidth = Float.parseFloat(widthField.getText());
                roomLength = Float.parseFloat(lengthField.getText());
                canvas.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid room size");
            }
        });
        controlPanel.add(applySizeButton);

        String[] shapes = { "Rectangular", "L-Shape (Basic)" };
        JComboBox<String> shapeBox = new JComboBox<>(shapes);
        shapeBox.addActionListener(e -> {
            roomShape = (String) shapeBox.getSelectedItem();
            canvas.repaint();
        });
        controlPanel.add(new JLabel("Shape:"));
        controlPanel.add(shapeBox);

        JButton viewToggleButton = new JButton("Toggle 2D/3D");
        viewToggleButton.addActionListener(e -> {
            is2DView = !is2DView;
            canvas.repaint();
        });
        controlPanel.add(viewToggleButton);

        JCheckBox scaleCheck = new JCheckBox("Scale to Fit");
        scaleCheck.addItemListener(e -> {
            autoScaleToFit = e.getStateChange() == ItemEvent.SELECTED;
            canvas.repaint();
        });
        controlPanel.add(scaleCheck);

        JCheckBox shadeCheck = new JCheckBox("Add Shade");
        shadeCheck.addItemListener(e -> {
            applyShade = e.getStateChange() == ItemEvent.SELECTED;
            canvas.repaint();
        });
        controlPanel.add(shadeCheck);

        // Leg Height Slider
        JSlider legHeightSlider = new JSlider(10, 200, (int) (legHeight * 100));
        legHeightSlider.setMajorTickSpacing(30);
        legHeightSlider.setMinorTickSpacing(10);
        legHeightSlider.setPaintTicks(true);
        legHeightSlider.setPaintLabels(true);
        legHeightSlider.addChangeListener(e -> {
            legHeight = legHeightSlider.getValue() / 100f;
            canvas.repaint();
        });
        controlPanel.add(new JLabel("Leg Height:"));
        controlPanel.add(legHeightSlider);

        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.NORTH);
        canvas.requestFocusInWindow();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        glut = new GLUT();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        if (isDaytime) {
            gl.glClearColor(0.96f, 0.96f, 0.86f, 1.0f);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[]{0.0f, 5.0f, 0.0f, 1.0f}, 0);
        } else {
            gl.glClearColor(0f, 0f, 0.1f, 1.0f);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[]{0.0f, 0.0f, 5.0f, 1.0f}, 0);
        }

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        if (!is2DView) {
            gl.glTranslatef(0f, 0f, -5f);
            gl.glRotatef(rotateX, 1, 0, 0);
            gl.glRotatef(rotateY, 0, 1, 0);
        }

        drawBed(gl);
        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0, (double) width / (double) height, 0.1, 100.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    private void drawLeg(GL2 gl) {
        gl.glPushMatrix();
        gl.glScalef(1f, legHeight / 0.2f, 1f);
        glut.glutSolidCube(0.2f);
        gl.glPopMatrix();
    }

    private void drawBed(GL2 gl) {
        float scaleFactor = autoScaleToFit ? Math.min(roomWidth / 2.5f, roomLength / 4f) : 1.0f;

        gl.glPushMatrix();
        gl.glScalef(scaleFactor, scaleFactor, scaleFactor);

        float[] bedColor = { bedColorR, bedColorG, bedColorB, 1.0f };
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, bedColor, 0);

        float lowestY = bedPositionY;

        gl.glPushMatrix(); gl.glTranslatef(0.7f + bedPositionX, lowestY + legHeight / 2f, 1.4f + bedPositionZ); drawLeg(gl); gl.glPopMatrix();
        gl.glPushMatrix(); gl.glTranslatef(-0.5f + bedPositionX, lowestY + legHeight / 2f, 1.4f + bedPositionZ); drawLeg(gl); gl.glPopMatrix();
        gl.glPushMatrix(); gl.glTranslatef(0.7f + bedPositionX, lowestY + legHeight / 2f, -1.4f + bedPositionZ); drawLeg(gl); gl.glPopMatrix();
        gl.glPushMatrix(); gl.glTranslatef(-0.5f + bedPositionX, lowestY + legHeight / 2f, -1.4f + bedPositionZ); drawLeg(gl); gl.glPopMatrix();

        float[] mattressColor = applyShade ? new float[]{0.7f, 0.7f, 0.7f, 1.0f} : new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, mattressColor, 0);

        gl.glPushMatrix();
        gl.glTranslatef(0.1f + bedPositionX, lowestY + legHeight + 0.15f, bedPositionZ);
        gl.glScalef(1.4f, 0.3f, 3.0f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        gl.glPopMatrix();

        gl.glColor3f(1, 1, 1);
        if (roomShape.equals("Rectangular")) {
            gl.glPushMatrix(); gl.glTranslatef(0.0f, lowestY - 0.5f, 0.0f); gl.glScalef(roomWidth, 0.1f, roomLength); glut.glutSolidCube(1); gl.glPopMatrix();
        } else {
            gl.glPushMatrix(); gl.glTranslatef(-roomWidth / 4, lowestY - 0.5f, 0.0f); gl.glScalef(roomWidth / 2, 0.1f, roomLength); glut.glutSolidCube(1); gl.glPopMatrix();
            gl.glPushMatrix(); gl.glTranslatef(roomWidth / 4, lowestY - 0.5f, -roomLength / 4); gl.glScalef(roomWidth / 2, 0.1f, roomLength / 2); glut.glutSolidCube(1); gl.glPopMatrix();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("3D Bed Design");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new SingleBed());
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}