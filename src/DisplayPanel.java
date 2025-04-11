import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.*;
import java.awt.Font;
import java.awt.Point;

public class DisplayPanel extends JPanel implements KeyListener, MouseListener, ActionListener {
    private int rectX;
    private int rectY;
    private Rectangle rect1;
    private int rect2X;
    private int rect2Y;
    private Rectangle rect2;

    private String message;
    private Color rectColor;

    public DisplayPanel() {
        rectX = 50;
        rectY = 30;
        rect1 = new Rectangle(70, 30);
        rect2X = 230;
        rect2Y = 5;
        rect2 = new Rectangle(20, 20);
        message = "mouse click: ";
        rectColor = Color.RED;

        // UPDATE timer to be 10ms, which will now trigger 100 times per second
        Timer timer = new Timer(10, this);
        timer.start();

        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        rect1.setLocation(rectX, rectY);
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(rectColor);
        g2d.draw(rect1);

        rect2.setLocation(rect2X, rect2Y);
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.BLUE);
        g2d.draw(rect2);

        g.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(Color.BLACK);
        g2d.drawString(message, 120, 150);
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        int currentX = (int) rect1.getX();
        int currentY = (int) rect1.getY();

        int key = e.getKeyCode();
        if (key == 38) {  // up key
            rectY = currentY - 5;
        } else if (key == 40) { // down key
            rectY = currentY + 5;
        } else if (key == 37) { // left key
            rectX = currentX - 5;
        } else if (key == 39) {  // right key
            rectX = currentX + 5;
        }

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            Point mouseClickLocation = e.getPoint();
            message = "mouse click: (" + mouseClickLocation.getX() + ", " + mouseClickLocation.getY() + ")";
            if (rect1.contains(mouseClickLocation)) {
                rectColor = Color.GREEN;
            } else {
                rectColor = Color.RED;
            }

            repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof Timer) {
            // timer now pulses ever 10ms (rather than 1 second), and you can use it to
            // move the second rectangle
            rect2Y += 1;  // move rect2 down 1 pixel evert 10ms

            // if y value exceeds height of window (defined in SampleFrame),
            // reset to starting position near top of screen
            if (rect2Y > 200) {
                rect2Y = 5;
            }

            // must call repaint to refresh the screen to show the new position of rect2
            repaint();
        }
    }
}