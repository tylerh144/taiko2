import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
    private int animCount, syncCount;
    private final double GAME_TICK = 15.5; //school: 10.54, home: 15.50-.54
    private Clip audio;
    private BufferedImage goodImg, missImg, drumIn, drumOut;
    private float d1a, d2a, k1a, k2a, goodA, missA;
    private double missY;

    private String message;

    public DisplayPanel() {

        timer = new Timer(1, this);
        curTime = ((int) (-2000 / GAME_TICK)) * GAME_TICK; //bus: 878000, override,shunran:-2000
        load = new SongLoader(GAME_TICK);
        song = load.getSong("sukisuki");
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
        syncCount = 0;

        message = "";
        close = true;

        try {
            goodImg = ImageIO.read(new File("Assets/good.png"));
            missImg = ImageIO.read(new File("Assets/miss.png"));
            drumIn = ImageIO.read(new File("Assets/taiko-drum-inner.png"));
            drumOut = ImageIO.read(new File("Assets/taiko-drum-outer.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

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
                        missA = 8f;
                        missY = 75;
                        if (combo > maxCombo) {
                            maxCombo = combo;
                        }
                        if (combo >= 50) {
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
                        combo = 0;
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

            if (goodA > 1) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            } else {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, goodA));
            }
            g2d.drawImage(goodImg, 132, 75, 200, 200, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

            if (missA > 1) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            } else {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, missA));
            }
            g2d.drawImage(missImg, 132, (int) missY, 200, 200, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

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
                d1a = 16f;
//                System.out.println("D1");
                hit(0);
            }
        } else if (key == KeyEvent.VK_G) {
            if (!d2Down) {
                d2Down = true;
                d2a = 16f;
//                System.out.println("D2");
                hit(0);
            }
        } else if (key == KeyEvent.VK_NUMPAD5) {
            if (!k1Down) {
                k1Down = true;
                k1a = 16f;
//                System.out.println("K1");
                hit(1);
            }
        } else if (key == KeyEvent.VK_NUMPAD6) {
            if (!k2Down) {
                k2Down = true;
                k2a = 16f;
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
            syncCount++;
            if (animCount == 20) {
                close = !close;
                animCount = 0;
            }
            if (syncCount == 100) {
                syncCount = 0;
                audio.setMicrosecondPosition((long) (1000 * curTime));
            }

            k1a *= .5f;
            k2a *= .5f;
            d1a *= .5f;
            d2a *= .5f;
            goodA *= .85f;
            missA *= .8f;
            missY += .5;
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
        g.setColor(Color.decode("#111111"));
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
        g.setColor(Color.BLACK);
        g.drawRect(0, 100, 1000, 150);

        g.setColor(Color.WHITE);
        g.drawOval(192, 135, 80, 80);

        g.setColor(Color.DARK_GRAY);
        g.fillOval(200, 143, 64, 64);
    }

    private void drawDrum(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.BLACK);
        g.fillRect(0, 100, 150, 150);

        g.setColor(Color.DARK_GRAY);
        g.fillOval(25, 125, 100, 100);

//MAYBE ABSTRACT THIS
        if (k1a > 1) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        } else {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, k1a));
        }
        g2d.drawImage(drumOut, 75, 125, -50, 100, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

        if (k2a > 1) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        } else {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, k2a));
        }
        g2d.drawImage(drumOut, 75, 125, 50, 100, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

        if (d1a > 1) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        } else {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, d1a));
        }
        g2d.drawImage(drumIn, 35, 135, 40, 80, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

        if (d2a > 1) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        } else {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, d2a));
        }
        g2d.drawImage(drumIn, 115, 135, -40, 80, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

        g.setColor(Color.BLACK);
        g.drawOval(25, 125, 100, 100);
        g.drawOval(35, 135, 80, 80);
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

//CHANGE HIT WINDOWS
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
                goodA = 2f;
                song.removeFirst();
                if (!song.isEmpty()) {
                    currentNote = song.getFirst();
                }
            }
        } else if (curHit < curTime + 100 && curHit > curTime - 100) {
            miss++;
            missA = 8f;
            missY = 75;
            if (combo >= 50) {
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
            combo = 0;
            song.removeFirst();
            if (!song.isEmpty()) {
                currentNote = song.getFirst();
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