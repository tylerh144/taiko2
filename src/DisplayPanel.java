import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

public class DisplayPanel extends JPanel implements KeyListener, MouseListener, ActionListener {
    private Timer timer;
    private double curTime;
    private boolean isMenu;
    private boolean d1Down, d2Down, k1Down, k2Down;
    private ArrayList<Note> song;
    private Note currentNote;
    private int perf, good, miss, combo, maxCombo;
    private double accuracy;
    private SongLoader load;
    private boolean close;
    private int animCount, d1Count, d2Count, k1Count, k2Count;
    private final double GAME_TICK = 15.53; //school: 10.54, home:
    private Clip audio;

    private String message;

    public DisplayPanel() {

        timer = new Timer(1, this);
        curTime = ((int) (-2000 / GAME_TICK)) * GAME_TICK; //bus: 878000, override,shunran:-2000
        load = new SongLoader(GAME_TICK);
        song = load.getSong("override");
        loadMusic();

        isMenu = false;
        d1Down = false;
        d2Down = false;
        k1Down = false;
        k2Down = false;

        currentNote = null;
        perf = 0;
        good = 0;
        miss = 0;
        combo = 0;
        maxCombo = 0;
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
            calcAcc();

            if (!song.isEmpty()) {
                currentNote = song.getFirst();
                for (int i = song.size() - 1; i >= 0; i--) {
                    Note n = song.get(i);
                    //add 200ms for real
                    if (n.getHitTime() < curTime - 100) {
                        song.remove(i);
                        miss++;
                        if (combo > maxCombo) {
                            maxCombo = combo;
                        }
                        combo = 0;
//MAYBE ADD ANOTHER MISS SOUND
                        File audioFile = new File("Assets/sound_combobreak.wav");
                        try {
                            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                            Clip clip = AudioSystem.getClip();
                            clip.open(audioStream);
                            clip.start();
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }

                        i++;
                    } else if (curTime >= n.getSpawnTime()) {

                        if (n.isBig()) {
                            g2d.drawImage(n.getImg(close), (int) n.getxPos() - 19, 124, 106, 106, null);
                        } else {
                            g2d.drawImage(n.getImg(close), (int) n.getxPos(), 143, 64, 64, null);

                        }
                        n.move();
                    }
                }
            }
            drawDrum(g);
//            else {
//                timer.stop();
//            }
        }

        g2d.drawString(message, 120, 150);
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    //FIX TIME DILATION WHEN HOLDING DOWN KEYS
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
            curTime+= GAME_TICK;
            if (curTime > 0 && !audio.isActive()) {
                audio.start();
            }
            animCount++;
            if (animCount == 20) {
                close = !close;
                animCount = 0;
            }
            repaint();
        }
    }

    private void drawLane(Graphics g) {
        //bg img
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(load.getBg(), 0, 140, 1000, (int) (1000 * load.getBgRatio()),  null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .8f));
        g.setColor(Color.BLACK);
        g2d.fillRect(0, 250, 1000, 325);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

        //note lane
        g.setColor(Color.BLACK);
        g.fillRect(0, 100, 1000, 150);

        //stats
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16)); //temp
        g.drawString("Perfect: " + perf, 50, 25);
        g.drawString("Good: " + good, 50, 50);
        g.drawString("Miss: " + miss, 50, 75);
        g.drawString("Accuracy: " + accuracy + "%", 200, 25);
        g.drawString("Max Combo: " + maxCombo, 200, 50);
        g.drawString("Current Time: " + curTime, 200, 75);

        //hit indicator
        g.setColor(Color.WHITE);
        g.drawOval(200, 143, 64, 64);
        g.drawRect(0, 100, 1000, 150);
    }

    private void drawDrum( Graphics g) {
        Color blue = Color.decode("#32b0be");
        Color red = Color.decode("#f9472d");
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 100, 150, 150);
        if (k1Down) {
            g.setColor(blue);
        } else {
            g.setColor(Color.DARK_GRAY);
        }
        g.fillArc(25, 125, 100, 100, 90, 180);
        if (k2Down) {
            g.setColor(blue);
        } else {
            g.setColor(Color.DARK_GRAY);
        }
        g.fillArc(25, 125, 100, 100, 270, 180);
        if (d1Down) {
            g.setColor(red);
        } else {
            g.setColor(Color.DARK_GRAY);
        }
        g.fillArc(38, 138, 74, 74, 90, 180);
        if (d2Down) {
            g.setColor(red);
        } else {
            g.setColor(Color.DARK_GRAY);
        }
        g.fillArc(38, 138, 74, 74, 270, 180);
        g.setColor(Color.BLACK);
        g.drawOval(25, 125, 100, 100);
        g.drawOval(38, 138, 74, 74);
        g.drawLine(75, 125, 75, 225);

        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.setColor(Color.WHITE);
        int comboX = 65;
        if (combo / 1000 > 0) {
            comboX = 42;
        } else if (combo / 100 > 0) {
            comboX = 52;
        } else if (combo / 10 > 0) {
            comboX = 60;
        }
        g.drawString("" + combo, comboX, 185);
    }

    private void hit(int keyColor) {
        double curHit = currentNote.getHitTime();
        int noteColor = currentNote.getColor();
        if (keyColor == noteColor) {
            if (curHit < curTime + 50 && curHit > curTime - 50) {
                perf++;
                combo++;
                song.removeFirst();
                if (!song.isEmpty()) {
                    currentNote = song.getFirst();
                }
            } else if (curHit < curTime + 100 && curHit > curTime - 100) {
                good++;
                combo++;
                song.removeFirst();
                if (!song.isEmpty()) {
                    currentNote = song.getFirst();
                }
            }
        } else if (curHit < curTime + 100 && curHit > curTime - 100) {
            miss++;
            combo = 0;
            song.removeFirst();
            if (!song.isEmpty()) {
                currentNote = song.getFirst();
            }

            File audioFile = new File("Assets/sound_combobreak.wav");
            try {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        if (combo > maxCombo) {
            maxCombo = combo;
        }

        String path = "";
        if (keyColor == 0) {
            path = "Assets/sound_don.wav";
        } else if (keyColor == 1) {
            path = "Assets/sound_ka.wav";
        }
        File audioFile = new File(path);
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void calcAcc() {
        double sumHit = perf + good * 2/3.0;
        double sumAll = perf + good + miss;
        accuracy = sumHit / sumAll * 10000;
        accuracy = Math.round(accuracy) / 100.0;
    }

    private void loadMusic() {
        File audioFile = new File(load.getAudioPath());
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            audio = AudioSystem.getClip();
            audio.open(audioStream);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}