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
import java.util.ArrayList;

public class DisplayPanel extends JPanel implements KeyListener, MouseListener, ActionListener {
    private Timer timer;
    private int curTime;
    private boolean isMenu;
    private boolean d1Down, d2Down, k1Down, k2Down;
    private ArrayList<Note> activeNotes, song;
    private Note currentNote;
    private int perf, good, miss;
    private double accuracy;
    private SongLoader load;
    private int counter;


    private String message;

    public DisplayPanel() {

        // UPDATE timer to be 10ms, which will now trigger 100 times per second
        timer = new Timer(10, this);
        curTime = 0;

        isMenu = false;

        d1Down = false;
        d2Down = false;
        k1Down = false;
        k2Down = false;



        song = new ArrayList<>();
        load = new SongLoader();
        song = load.getSong("phony");
        activeNotes = new ArrayList<>();

        for (Note n : song) {
            if (n.getSpawnTime() == 0) {
                activeNotes.add(n);
            }
        }

        currentNote = null;
        perf = 0;
        good = 0;
        miss = 0;
        accuracy = 0;

        counter = 0;

        message = "";

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

        if (!isMenu) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 200, 1000, 150);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("" + curTime, 100, 100);

            for (int i = activeNotes.size() - 1; i >= 0; i--) {
                Note n = activeNotes.get(i);
//                if (n.getHitTime() < curTime + 200) {
//                    activeNotes.remove(i);
//                    i++;
//                } else {
                    if (n.getColor() == 0) {
                        g.setColor(Color.RED);
                    } else {
                        g.setColor(Color.BLUE);
                    }

                    //draw note
                    g.fillOval(n.getxPos(), 250, 50, 50);
                    g.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawOval(n.getxPos(), 250, 50, 50);
                    n.move();
//                }
            }
        }

        g.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(Color.BLACK);
        g2d.drawString(message, 120, 150);
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_F) {
            if (!d1Down) {
                d1Down = true;
                System.out.println("D1");
            }
        } else if (key == KeyEvent.VK_G) {
            if (!d2Down) {
                d2Down = true;
                System.out.println("D2");
            }
        } else if (key == KeyEvent.VK_NUMPAD5) {
            if (!k1Down) {
                k1Down = true;
                System.out.println("K1");
            }
        } else if (key == KeyEvent.VK_NUMPAD6) {
            if (!k2Down) {
                k2Down = true;
                System.out.println("K2");
            }
        }

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_F) {
            d1Down = false;
        } else if (key == KeyEvent.VK_G) {
            d2Down = false;
        } else if (key == KeyEvent.VK_NUMPAD5) {
            k1Down = false;
        } else if (key == KeyEvent.VK_NUMPAD6) {
            k2Down = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof Timer) {
            curTime+=1;
            if (counter < song.size() && song.get(counter).getSpawnTime() >= curTime) {
                activeNotes.add(song.get(counter));
                counter++;
            }

            repaint();
        }
    }
}