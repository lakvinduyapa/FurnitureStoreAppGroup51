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

public class RectangulatopTable extends JPanel implements GLEventListener {

    private GLU glu;
    private GLUT glut;
    private float rotateX, rotateY;
    private int lastX, lastY;

    private float tableColorR = 0.54f;
    private float tableColorG = 0.27f;
    private float tableColorB = 0.07f;

    private float roomWidth = 2.0f;
    private float roomDepth = 2.0f;
    private String roomShape = "Rectangle";

    private boolean isDaytime = true;
    private boolean is3D = true;
    private boolean shadingEnabled = true;
    private boolean scaleToFit = false;

    private float modelTransX = 0f;
    private float modelTransZ = 0f;
    private boolean modelDragging = false;
    private int dragStartX, dragStartY;

    private GLCanvas canvas;

    public RectangulatopTable() {
        // Setup OpenGL canvas
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        canvas = new GLCanvas(caps);
        canvas.addGLEventListener(this);

        // Mouse listeners for rotation and dragging
        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    modelDragging = true;
                    dragStartX = e.getX();
                    dragStartY = e.getY();
                }
            }
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    modelDragging = false;
                }
            }
        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (modelDragging) {
                    int dx = e.getX() - dragStartX;
                    int dz = e.getY() - dragStartY;
                    modelTransX += dx * 0.01f;
                    modelTransZ += dz * 0.01f;
                    dragStartX = e.getX();
                    dragStartY = e.getY();
                } else {
                    rotateX += e.getY() - lastY;
                    rotateY += e.getX() - lastX;
                }
                lastX = e.getX(); lastY = e.getY();
                canvas.repaint();
            }
            public void mouseMoved(MouseEvent e) {
                lastX = e.getX(); lastY = e.getY();
            }
        });

        // Control panel
        Panel controlPanel = new Panel(new FlowLayout(FlowLayout.LEFT));

        // Color sliders
        JSlider redSlider = new JSlider(0,255,(int)(tableColorR*255));
        redSlider.addChangeListener(e -> { tableColorR = redSlider.getValue()/255f; canvas.repaint(); });
        controlPanel.add(new JLabel("R:")); controlPanel.add(redSlider);

        JSlider greenSlider = new JSlider(0,255,(int)(tableColorG*255));
        greenSlider.addChangeListener(e -> { tableColorG = greenSlider.getValue()/255f; canvas.repaint(); });
        controlPanel.add(new JLabel("G:")); controlPanel.add(greenSlider);

        JSlider blueSlider = new JSlider(0,255,(int)(tableColorB*255));
        blueSlider.addChangeListener(e -> { tableColorB = blueSlider.getValue()/255f; canvas.repaint(); });
        controlPanel.add(new JLabel("B:")); controlPanel.add(blueSlider);

        // Day/Night toggle
        JButton dayNightBtn = new JButton("Day/Night");
        dayNightBtn.addActionListener(e -> { isDaytime = !isDaytime; canvas.repaint(); });
        controlPanel.add(dayNightBtn);

        // 2D/3D toggle
        JToggleButton viewToggle = new JToggleButton("2D/3D", true);
        viewToggle.addActionListener(e -> { is3D = viewToggle.isSelected(); canvas.repaint(); });
        controlPanel.add(viewToggle);

        // Shading toggle
        JToggleButton shadingToggle = new JToggleButton("Shading", true);
        shadingToggle.addActionListener(e -> { shadingEnabled = shadingToggle.isSelected(); canvas.repaint(); });
        controlPanel.add(shadingToggle);

        // Scale Fit toggle
        JToggleButton scaleToggle = new JToggleButton("Scale Fit");
        scaleToggle.addActionListener(e -> { scaleToFit = scaleToggle.isSelected(); canvas.repaint(); });
        controlPanel.add(scaleToggle);

        // Room shape selector
        JComboBox<String> shapeSelector = new JComboBox<>(new String[]{"Rectangle","L-Shape"});
        shapeSelector.addActionListener(e -> { roomShape = (String)shapeSelector.getSelectedItem(); canvas.repaint(); });
        controlPanel.add(new JLabel("Shape:")); controlPanel.add(shapeSelector);

        // Room size inputs
        JTextField widthField = new JTextField(String.valueOf(roomWidth),4);
        JTextField depthField = new JTextField(String.valueOf(roomDepth),4);
        JButton applyRoomBtn = new JButton("Apply Size");
        applyRoomBtn.addActionListener(e -> {
            try {
                roomWidth = Float.parseFloat(widthField.getText());
                roomDepth = Float.parseFloat(depthField.getText());
                canvas.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid dimensions", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        controlPanel.add(new JLabel("W:")); controlPanel.add(widthField);
        controlPanel.add(new JLabel("D:")); controlPanel.add(depthField);
        controlPanel.add(applyRoomBtn);

        // Save and Reset
        JButton saveBtn = new JButton("Save PNG");
        saveBtn.addActionListener(e -> saveImage());
        controlPanel.add(saveBtn);

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> { resetView(); canvas.repaint(); });
        controlPanel.add(resetBtn);

        // Layout
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
    }

    private void saveImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            BufferedImage img = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics(); canvas.paint(g2); g2.dispose();
            try { ImageIO.write(img, "PNG", new File(file.getAbsolutePath()+".png")); }
            catch (IOException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
        }
    }

    private void resetView() {
        rotateX = rotateY = 0;
        modelTransX = modelTransZ = 0;
        isDaytime = is3D = shadingEnabled = scaleToFit = true;
        roomWidth = roomDepth = 2.0f;
        roomShape = "Rectangle";
    }

    @Override public void init(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2(); glu=new GLU(); glut=new GLUT();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[]{0f,5f,0f,1f}, 0);
        resetView();
    }

    @Override public void dispose(GLAutoDrawable d) {}

    @Override public void display(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2();

        // Background
        if (isDaytime) gl.glClearColor(0.96f,0.96f,0.86f,1f);
        else gl.glClearColor(0f,0f,0.1f,1f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        // Camera
        if (is3D) { gl.glTranslatef(0,0,-5); gl.glRotatef(rotateX,1,0,0); gl.glRotatef(rotateY,0,1,0); }
        else { glu.gluLookAt(5,0,0,0,0,0,0,1,0); }

        // Scale fit
        if (scaleToFit) {
            float max = Math.max(roomWidth, roomDepth);
            float s = 2.0f / max;
            gl.glScalef(s,s,s);
        }

        // Draw floor
        gl.glColor3f(1f,1f,1f);
        if (roomShape.equals("Rectangle")) {
            gl.glPushMatrix(); gl.glTranslatef(0,-1.5f,0); gl.glScalef(roomWidth,0.1f,roomDepth); glut.glutSolidCube(1f); gl.glPopMatrix();
        } else {
            gl.glPushMatrix(); gl.glTranslatef(-roomWidth/4f,-1.5f,0); gl.glScalef(roomWidth/2f,0.1f,roomDepth); glut.glutSolidCube(1f); gl.glPopMatrix();
            gl.glPushMatrix(); gl.glTranslatef(roomWidth/4f,-1.5f,-roomDepth/4f); gl.glScalef(roomWidth,0.1f,roomDepth/2f); glut.glutSolidCube(1f); gl.glPopMatrix();
        }

        // Draw model
        gl.glPushMatrix(); gl.glTranslatef(modelTransX,0,modelTransZ);
        gl.glShadeModel(shadingEnabled?GL2.GL_SMOOTH:GL2.GL_FLAT);
        gl.glColor3f(tableColorR,tableColorG,tableColorB);
        gl.glPushMatrix(); gl.glTranslatef(0,-0.45f,0); gl.glScalef(1.5f,0.05f,1f); glut.glutSolidCube(1f); gl.glPopMatrix();
        for(int i=0;i<4;i++){ double a=i*Math.PI/2; float x=(float)Math.cos(a)*0.4f, z=(float)Math.sin(a)*0.4f;
            gl.glPushMatrix(); gl.glTranslatef(x,-0.9f,z); gl.glScalef(0.1f,0.9f,0.1f); glut.glutSolidCube(1f); gl.glPopMatrix(); }
        gl.glPopMatrix();

        gl.glFlush();
    }

    @Override public void reshape(GLAutoDrawable d,int x,int y,int w,int h){
        GL2 gl=d.getGL().getGL2(); gl.glViewport(0,0,w,h);
        gl.glMatrixMode(GL2.GL_PROJECTION); gl.glLoadIdentity();
        double asp=(double)w/h;
        if(is3D) glu.gluPerspective(45,asp,0.1,100);
        else { float size=Math.max(roomWidth,roomDepth); gl.glOrtho(-size*asp,size*asp,-size,size,-10,10);}
        gl.glMatrixMode(GL2.GL_MODELVIEW); gl.glLoadIdentity(); if(is3D) glu.gluLookAt(0,0,5,0,0,0,0,1,0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Rectangular Table");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200,1000);
            RectangulatopTable panel = new RectangulatopTable();
            frame.add(panel);
            frame.setVisible(true);
            panel.canvas.requestFocusInWindow();
        });
    }
}
