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

public class OfficeDesk extends JPanel implements GLEventListener {
    private GLU glu;
    private GLUT glut;
    private float rotateX, rotateY;
    private boolean isDragging = false;
    private boolean shiftDown = false;
    private int lastX, lastY;

    private float deskColorR = 0.55f, deskColorG = 0.27f, deskColorB = 0.07f;
    private boolean isDaytime = true;
    private boolean is3DView = true;
    private boolean shadingEnabled = false;
    private boolean scaleFitEnabled = false;

    private float roomWidth = 3.0f, roomLength = 2.0f, roomHeight = 2.5f;
    private String roomShape = "Rectangle";

    private float deskTranslateX = 0.0f, deskTranslateZ = 0.0f;

    public OfficeDesk() {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(caps);
        canvas.addGLEventListener(this);

        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                isDragging = true;
                lastX = e.getX();
                lastY = e.getY();
            }

            public void mouseReleased(MouseEvent e) {
                isDragging = false;
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastX;
                int dy = e.getY() - lastY;
                if (shiftDown) {
                    rotateX += dy;
                    rotateY += dx;
                } else {
                    float angleRad = (float) Math.toRadians(rotateY);
                    deskTranslateX += dx * 0.01f * Math.cos(angleRad) - dy * 0.01f * Math.sin(angleRad);
                    deskTranslateZ += dx * 0.01f * Math.sin(angleRad) + dy * 0.01f * Math.cos(angleRad);
                }
                lastX = e.getX();
                lastY = e.getY();
                canvas.repaint();
            }
        });

        canvas.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) shiftDown = true;
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) shiftDown = false;
            }
        });

        canvas.setFocusable(true);
        canvas.requestFocusInWindow();

        setLayout(new BorderLayout());
        JPanel control = new JPanel();

        JTextField widthField = new JTextField(Float.toString(roomWidth), 4);
        JTextField lengthField = new JTextField(Float.toString(roomLength), 4);
        control.add(new JLabel("Room Width:"));
        control.add(widthField);
        control.add(new JLabel("Room Length:"));
        control.add(lengthField);

        JButton applySize = new JButton("Apply Room Size");
        applySize.addActionListener(e -> {
            try {
                roomWidth = Float.parseFloat(widthField.getText());
                roomLength = Float.parseFloat(lengthField.getText());
                canvas.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid room dimensions.");
            }
        });
        control.add(applySize);

        JComboBox<String> shapeBox = new JComboBox<>(new String[]{"Rectangle", "L-Shape"});
        shapeBox.addActionListener(e -> {
            roomShape = (String) shapeBox.getSelectedItem();
            canvas.repaint();
        });
        control.add(new JLabel("Room Shape:"));
        control.add(shapeBox);

        JSlider rS = new JSlider(0, 255, (int) (deskColorR * 255));
        JSlider gS = new JSlider(0, 255, (int) (deskColorG * 255));
        JSlider bS = new JSlider(0, 255, (int) (deskColorB * 255));
        rS.addChangeListener(e -> {
            deskColorR = rS.getValue() / 255f;
            canvas.repaint();
        });
        gS.addChangeListener(e -> {
            deskColorG = gS.getValue() / 255f;
            canvas.repaint();
        });
        bS.addChangeListener(e -> {
            deskColorB = bS.getValue() / 255f;
            canvas.repaint();
        });
        control.add(new JLabel("R:"));
        control.add(rS);
        control.add(new JLabel("G:"));
        control.add(gS);
        control.add(new JLabel("B:"));
        control.add(bS);

        JCheckBox shadeToggle = new JCheckBox("Enable Shading");
        shadeToggle.addActionListener(e -> {
            shadingEnabled = shadeToggle.isSelected();
            canvas.repaint();
        });
        control.add(shadeToggle);

        JCheckBox scaleToggle = new JCheckBox("Scale Fit");
        scaleToggle.addActionListener(e -> {
            scaleFitEnabled = scaleToggle.isSelected();
            canvas.repaint();
        });
        control.add(scaleToggle);

        JButton bgBtn = new JButton("Day/Night");
        bgBtn.addActionListener(e -> {
            isDaytime = !isDaytime;
            canvas.repaint();
        });
        control.add(bgBtn);

        JButton viewBtn = new JButton("Toggle 2D/3D");
        viewBtn.addActionListener(e -> {
            is3DView = !is3DView;
            canvas.repaint();
        });
        control.add(viewBtn);

        JButton saveBtn = new JButton("Save PNG");
        saveBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedImage img = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = img.createGraphics();
                    canvas.paint(g2);
                    g2.dispose();
                    ImageIO.write(img, "PNG", new File(fc.getSelectedFile() + ".png"));
                    JOptionPane.showMessageDialog(null, "Saved!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Save failed");
                }
            }
        });
        control.add(saveBtn);

        JButton delBtn = new JButton("Delete Design");
        delBtn.addActionListener(e -> {
            rotateX = rotateY = 0;
            deskTranslateX = deskTranslateZ = 0;
            isDaytime = true;
            canvas.repaint();
        });
        control.add(delBtn);

        add(canvas, BorderLayout.CENTER);
        add(control, BorderLayout.NORTH);
    }

    @Override
    public void init(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2();
        glu = new GLU();
        glut = new GLUT();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        float[] lightPos = {0, 5, 0, 1};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
    }

    @Override
    public void dispose(GLAutoDrawable d) {}

    @Override
    public void display(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2();
        Color bg = isDaytime ? new Color(245, 245, 235) : new Color(10, 10, 30);
        gl.glClearColor(bg.getRed() / 255f, bg.getGreen() / 255f, bg.getBlue() / 255f, 1f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        if (is3DView) {
            gl.glTranslatef(0, 0, -8);
            gl.glRotatef(rotateX, 1, 0, 0);
            gl.glRotatef(rotateY, 0, 1, 0);
        }

        drawFloor(gl);

        gl.glPushMatrix();
        gl.glTranslatef(deskTranslateX, 0f, deskTranslateZ);
        if (scaleFitEnabled) {
            float s = Math.min(roomWidth, roomLength) / 3f;
            gl.glScalef(s, s, s);
        }

        if (shadingEnabled) {
            float[] col = {deskColorR, deskColorG, deskColorB, 1f};
            gl.glEnable(GL2.GL_LIGHTING);
            gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, col, 0);
        } else {
            gl.glDisable(GL2.GL_LIGHTING);
            gl.glColor3f(deskColorR, deskColorG, deskColorB);
        }

        drawTable(gl);

        if (!shadingEnabled) gl.glEnable(GL2.GL_LIGHTING);
        gl.glPopMatrix();

        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
        GL2 gl = d.getGL().getGL2();
        gl.glViewport(0, 0, w, h);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        if (is3DView) {
            glu.gluPerspective(45, (double) w / h, 1, 100);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
            glu.gluLookAt(0, 0, 8, 0, 0, 0, 0, 1, 0);
        } else {
            float o = 10f;
            gl.glOrtho(-o, o, -o * h / w, o * h / w, -20, 20);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
        }
    }

    private void drawTable(GL2 gl) {
        gl.glPushMatrix();
        gl.glTranslatef(0, -0.5f, 0);
        gl.glScalef(1.5f, 0.1f, 1.0f);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        for (float x : new float[]{-0.6f, 0.6f})
            for (float z : new float[]{-0.4f, 0.4f}) {
                gl.glPushMatrix();
                gl.glTranslatef(x, -1f, z);
                gl.glScalef(0.1f, 1f, 0.1f);
                glut.glutSolidCube(1);
                gl.glPopMatrix();
            }
    }

    private void drawFloor(GL2 gl) {
        float fy = -1.5f;
        gl.glColor3f(1, 1, 1);
        float[] floorMat = {0.5f, 0.5f, 0.5f, 1};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, floorMat, 0);

        if ("Rectangle".equals(roomShape)) {
            gl.glPushMatrix();
            gl.glTranslatef(0, fy, 0);
            gl.glScalef(roomWidth, 0.1f, roomLength);
            glut.glutSolidCube(1);
            gl.glPopMatrix();
        } else {
            gl.glPushMatrix();
            gl.glTranslatef(-roomWidth / 4, fy, 0);
            gl.glScalef(roomWidth / 2, 0.1f, roomLength);
            glut.glutSolidCube(1);
            gl.glPopMatrix();

            gl.glPushMatrix();
            gl.glTranslatef(roomWidth / 4, fy, -roomLength / 4);
            gl.glScalef(roomWidth / 2, 0.1f, roomLength / 2);
            glut.glutSolidCube(1);
            gl.glPopMatrix();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Office Desk");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1200, 1000);
            f.setLocationRelativeTo(null);
            f.add(new OfficeDesk());
            f.setVisible(true);
        });
    }
}
