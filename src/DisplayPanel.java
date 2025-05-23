import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class DisplayPanel extends JPanel implements KeyListener, MouseListener, ActionListener {
    private Timer timer;
    private double curTime;
    private boolean isMenu;
    private boolean d1Down, d2Down, k1Down, k2Down;
    private ArrayList<Note> song;
    private Note currentNote;
    private int perf, good, miss;
    private double accuracy;
    private SongLoader load;
    private boolean close;
    private int animCount, d1Count, d2Count, k1Count, k2Count;


    private String message;

    public DisplayPanel() {

        timer = new Timer(1, this);
        curTime = 0;

        isMenu = false;

        d1Down = false;
        d2Down = false;
        k1Down = false;
        k2Down = false;



        load = new SongLoader();
        song = load.getSong("DNA");

        currentNote = null;
        perf = 0;
        good = 0;
        miss = 0;
        accuracy = 0;

        animCount = 0;

        message = "";
        close = true;

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
            drawLane(g);


            if (!song.isEmpty()) {
                currentNote = song.getFirst();
                for (int i = song.size() - 1; i >= 0; i--) {
                    Note n = song.get(i);
                    //add 200ms for real
                    if (n.getHitTime() < curTime - 100) {
                        song.remove(i);
                        miss++;
                        i++;
                    } else if (curTime >= n.getSpawnTime()) {
                        if (n.getColor() == 0) {
                            g.setColor(Color.RED);
                        } else {
                            g.setColor(Color.BLUE);
                        }
                        if (n.isBig()) {
                            g2d.drawImage(n.getImg(close), (int) n.getxPos() - 19, 124, null);
                        } else {
                            g2d.drawImage(n.getImg(close), (int) n.getxPos(), 143, null);

                        }
                        n.move();
                    }
                }
            }
//            else {
//                timer.stop();
//            }
        }

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
//                System.out.println("D1");
                hit(0);
            }
        } else if (key == KeyEvent.VK_G) {
            if (!d2Down) {
                d2Down = true;
//                System.out.println("D2");
                hit(0);
            }
        } else if (key == KeyEvent.VK_NUMPAD5) {
            if (!k1Down) {
                k1Down = true;
//                System.out.println("K1");
                hit(1);
            }
        } else if (key == KeyEvent.VK_NUMPAD6) {
            if (!k2Down) {
                k2Down = true;
//                System.out.println("K2");
                hit(1);
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
            curTime+=10; //15.55 pc, 10 school
            animCount++;
            if (animCount == 20) {
                close = !close;
                animCount = 0;
            }
            repaint();
        }
    }

    private void drawLane(Graphics g) {
        //note lane
        g.setColor(Color.BLACK);
        g.fillRect(0, 100, 1000, 150);

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(load.getBg(), 0, 250, 1000, 325,  null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .8f));
        g.setColor(Color.BLACK);
        g2d.fillRect(0, 250, 1000, 325);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16)); //temp
        g.drawString("" + curTime, 200, 50);
        g.drawString("Perfect: " + perf, 50, 25);
        g.drawString("Good: " + good, 50, 50);
        g.drawString("Miss: " + miss, 50, 75);
        g.drawString("Accuracy: " + accuracy, 200, 25);

        //square with drum
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 100, 150, 150);
        if (k1Down) {
            g.setColor(Color.BLUE);
        } else {
            g.setColor(Color.DARK_GRAY);
        }
        g.fillArc(25, 125, 100, 100, 90, 180);
        if (k2Down) {
            g.setColor(Color.BLUE);
        } else {
            g.setColor(Color.DARK_GRAY);
        }
        g.fillArc(25, 125, 100, 100, 270, 180);
        if (d1Down) {
            g.setColor(Color.RED);
        } else {
            g.setColor(Color.DARK_GRAY);
        }
        g.fillArc(38, 138, 74, 74, 90, 180);
        if (d2Down) {
            g.setColor(Color.RED);
        } else {
            g.setColor(Color.DARK_GRAY);
        }
        g.fillArc(38, 138, 74, 74, 270, 180);
        g.setColor(Color.BLACK);
        g.drawOval(25, 125, 100, 100);
        g.drawOval(38, 138, 74, 74);
        g.drawLine(75, 125, 75, 225);

        g.setColor(Color.WHITE);
        g.drawOval(200, 143, 64, 64);
        g.drawRect(0, 100, 1000, 150);
    }

    private void hit(int keyColor) {
        double curHit = currentNote.getHitTime();
        int noteColor = currentNote.getColor();
        if (keyColor == noteColor) {
            if (curHit < curTime + 60 && curHit > curTime - 60) {
                perf++;
                song.removeFirst();
                currentNote = song.getFirst();
            } else if (curHit < curTime + 150 && curHit > curTime - 100) {
                good++;
                song.removeFirst();
                currentNote = song.getFirst();
            }
        } else if (curHit < curTime + 150 && curHit > curTime - 100) {
            miss++;
            song.removeFirst();
            currentNote = song.getFirst();
        }


    }
}