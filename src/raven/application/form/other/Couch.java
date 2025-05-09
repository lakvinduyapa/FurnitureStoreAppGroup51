package raven.application.form.other;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Couch extends JPanel implements GLEventListener {
    private GLU glu;
    private GLUT glut;
    private float rotateX, rotateY;
    private float couchColorR = 0.7f, couchColorG = 0.5f, couchColorB = 0.3f;
    private float couchX = 0, couchY = -1.2f, couchZ = 0;
    private boolean isDaytime = true, isDragging = false, isRotating = false;
    private boolean is2DView = false, autoScale = false, shading = false, autoRotate = false;
    private float roomWidth = 5.0f, roomLength = 5.0f;
    private Color roomColor = new Color(240, 240, 220);
    private int lastX, lastY;
    private String roomShape = "Rectangle";
    private Timer rotateTimer;

    public Couch() {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        GLJPanel canvas = new GLJPanel(caps);
        canvas.addGLEventListener(this);

        rotateTimer = new Timer(30, e -> {
            if (autoRotate && !is2DView) {
                rotateY += 1;
                canvas.repaint();
            }
        });
        rotateTimer.start();

        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
                isDragging = SwingUtilities.isLeftMouseButton(e);
                isRotating = SwingUtilities.isRightMouseButton(e);
            }
            public void mouseReleased(MouseEvent e) {
                isDragging = isRotating = false;
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastX, dy = e.getY() - lastY;
                if (isRotating && !is2DView) {
                    rotateX += dy; rotateY += dx;
                } else if (isDragging) {
                    couchX += dx * 0.01f;
                    couchZ -= dy * 0.01f;
                }
                lastX = e.getX(); lastY = e.getY();
                canvas.repaint();
            }
        });

        canvas.addMouseWheelListener(e -> {
            couchY += e.getWheelRotation() * 0.1f;
            canvas.repaint();
        });

        JPanel controls = new JPanel();
        JSlider r = new JSlider(0, 255, (int)(couchColorR*255));
        r.addChangeListener(e -> { couchColorR = r.getValue()/255f; canvas.repaint(); });
        controls.add(new JLabel("R:")); controls.add(r);

        JSlider g = new JSlider(0, 255, (int)(couchColorG*255));
        g.addChangeListener(e -> { couchColorG = g.getValue()/255f; canvas.repaint(); });
        controls.add(new JLabel("G:")); controls.add(g);

        JSlider b = new JSlider(0, 255, (int)(couchColorB*255));
        b.addChangeListener(e -> { couchColorB = b.getValue()/255f; canvas.repaint(); });
        controls.add(new JLabel("B:")); controls.add(b);

        JCheckBox viewToggle = new JCheckBox("2D View");
        viewToggle.addActionListener(e -> { is2DView = viewToggle.isSelected(); canvas.repaint(); });
        controls.add(viewToggle);

        JCheckBox scale = new JCheckBox("Auto Scale");
        scale.addActionListener(e -> { autoScale = scale.isSelected(); canvas.repaint(); });
        controls.add(scale);

        JCheckBox shade = new JCheckBox("Shading");
        shade.addActionListener(e -> { shading = shade.isSelected(); canvas.repaint(); });
        controls.add(shade);

        JCheckBox autoRot = new JCheckBox("360 View");
        autoRot.addActionListener(e -> autoRotate = autoRot.isSelected());
        controls.add(autoRot);

        JButton bgToggle = new JButton("Day/Night");
        bgToggle.addActionListener(e -> { isDaytime = !isDaytime; canvas.repaint(); });
        controls.add(bgToggle);

        JButton roomColorBtn = new JButton("Room Color");
        roomColorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(null, "Choose Room Color", roomColor);
            if (newColor != null) roomColor = newColor;
            canvas.repaint();
        });
        controls.add(roomColorBtn);

        JComboBox<String> shapeBox = new JComboBox<>(new String[]{"Rectangle","L-Shape"});
        shapeBox.addActionListener(e -> { roomShape = (String)shapeBox.getSelectedItem(); canvas.repaint(); });
        controls.add(new JLabel("Room Shape:")); controls.add(shapeBox);

        JTextField widthField = new JTextField(""+roomWidth,4),
                lengthField = new JTextField(""+roomLength,4);
        JButton apply = new JButton("Apply Room Size");
        apply.addActionListener(e -> {
            try {
                roomWidth = Float.parseFloat(widthField.getText());
                roomLength = Float.parseFloat(lengthField.getText());
                canvas.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null,"Invalid room size");
            }
        });
        controls.add(new JLabel("W:")); controls.add(widthField);
        controls.add(new JLabel("L:")); controls.add(lengthField);
        controls.add(apply);

        JButton save = new JButton("Save PNG");
        save.addActionListener(e -> {
            BufferedImage img = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            canvas.paint(g2);
            try {
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION) {
                    ImageIO.write(img,"PNG",new File(fc.getSelectedFile()+".png"));
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,"Save failed: "+ex.getMessage());
            }
        });
        controls.add(save);

        JButton reset = new JButton("Reset");
        reset.addActionListener(e -> {
            rotateX=rotateY=0;
            couchX=couchZ=0; couchY=-1.2f;
            isDaytime=is2DView=autoScale=shading=autoRotate=false;
            couchColorR=0.7f; couchColorG=0.5f; couchColorB=0.3f;
            roomWidth=5f; roomLength=5f; roomColor=new Color(240,240,220);
            canvas.repaint();
        });
        controls.add(reset);

        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        add(controls, BorderLayout.NORTH);
    }

    @Override
    public void init(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2();
        glu = new GLU(); glut = new GLUT();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);

        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
    }

    @Override public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2();
        Color bg = isDaytime ? new Color(245,245,235) : new Color(10,10,30);
        gl.glClearColor(bg.getRed()/255f, bg.getGreen()/255f, bg.getBlue()/255f,1);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT|GL.GL_DEPTH_BUFFER_BIT);

        gl.glLoadIdentity();
        gl.glTranslatef(0,0,-8);
        if (!is2DView) {
            gl.glRotatef(rotateX,1,0,0);
            gl.glRotatef(rotateY,0,1,0);
        }

        // Room floor
        gl.glColor3f(roomColor.getRed()/255f, roomColor.getGreen()/255f, roomColor.getBlue()/255f);
        if (roomShape.equals("Rectangle")) {
            gl.glPushMatrix();
            gl.glTranslatef(0,-2f,0);
            gl.glScalef(roomWidth,0.1f,roomLength);
            glut.glutSolidCube(1);
            gl.glPopMatrix();
        } else {
            gl.glPushMatrix();
            gl.glTranslatef(-roomWidth/4,-2f,0);
            gl.glScalef(roomWidth/2,0.1f,roomLength);
            glut.glutSolidCube(1);
            gl.glPopMatrix();
            gl.glPushMatrix();
            gl.glTranslatef(roomWidth/4,-2f,-roomLength/4);
            gl.glScalef(roomWidth/2,0.1f,roomLength/2);
            glut.glutSolidCube(1);
            gl.glPopMatrix();
        }

        // Draw Couch
        float scale = autoScale ? Math.min(roomWidth/2f,roomLength/2f) : 1f;
        gl.glPushMatrix();
        gl.glTranslatef(couchX,couchY,couchZ);
        gl.glColor3f(couchColorR, couchColorG, couchColorB);

        // Seat
        gl.glPushMatrix();
        gl.glTranslatef(0,0.25f*scale,0);
        gl.glScalef(2.5f*scale,0.5f*scale,1.2f*scale);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        // Backrest
        gl.glPushMatrix();
        gl.glTranslatef(0,0.95f*scale,-0.6f*scale);
        gl.glScalef(2.5f*scale,1.0f*scale,0.2f*scale);
        glut.glutSolidCube(1);
        gl.glPopMatrix();

        // Armrests
        for (float dx : new float[]{-1.35f,1.35f}) {
            gl.glPushMatrix();
            gl.glTranslatef(dx*scale,0.6f*scale,0);
            gl.glScalef(0.2f*scale,1.0f*scale,1.2f*scale);
            glut.glutSolidCube(1);
            gl.glPopMatrix();
        }

        // Legs (black)
        gl.glColor3f(0,0,0);
        for (float dx : new float[]{-1.0f,1.0f}) {
            for (float dz : new float[]{-0.5f,0.5f}) {
                gl.glPushMatrix();
                gl.glTranslatef(dx*scale,-0.1f*scale,dz*scale);
                gl.glScalef(0.2f*scale,0.2f*scale,0.2f*scale);
                glut.glutSolidCube(1);
                gl.glPopMatrix();
            }
        }

        gl.glPopMatrix();
        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable d, int x, int y, int width, int height) {
        GL2 gl = d.getGL().getGL2();
        gl.glViewport(0,0,width,height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45,(double)width/height,0.1,100.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("3D Couch");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,600);
        frame.setContentPane(new Couch());
        frame.setVisible(true);
    }
}