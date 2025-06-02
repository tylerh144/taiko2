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
    private double curTime, endTime;
    private boolean isMenu, isEnd, isGame;
    private boolean d1Down, d2Down, k1Down, k2Down;
    private ArrayList<Note> song;
    private Note currentNote;
    private int perf, good, miss, combo, maxCombo;
    private double accuracy;
    private SongLoader load;
    private boolean close;
    private int animCount;
    private Clip audio;
    private BufferedImage goodImg, missImg, drumIn, drumOut;
    private float d1a, d2a, k1a, k2a, goodA, missA;
    private double missY;
    private Rectangle back, play;
    private String songName;

    public DisplayPanel() {
        timer = new Timer(1, this);

        load = new SongLoader();

        songName = "DNA";

        reset();

        isMenu = true;
        isGame = false;
        isEnd = false;

        //MAKE THESE INTO REAL BUTTONS
        back = new Rectangle(25, 500, 250, 50);
        play = new Rectangle(725, 500, 250, 50);

        try {
            goodImg = ImageIO.read(new File("Assets/good.png"));
            missImg = ImageIO.read(new File("Assets/miss.png"));
            drumIn = ImageIO.read(new File("Assets/taiko-drum-inner.png"));
            drumOut = ImageIO.read(new File("Assets/taiko-drum-outer.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (isGame) {
            drawLane(g);
            calcAcc();

            if (!song.isEmpty()) {
                currentNote = song.getFirst();
                for (int i = song.size() - 1; i >= 0; i--) {
                    if (!song.isEmpty()) {
                        Note n = song.get(i);
                        if (n.getHitTime() < curTime - 100) {
                            song.remove(i);
                            miss++;
                            animCount = 0;
                            close = true;
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
                            n.move(curTime);
                        }
                    }
                }
            }

            drawDrum(g);

            drawFade(g2d, goodImg, goodA, 132, 75, 200, 200);
            drawFade(g2d, missImg, missA, 132, (int) missY, 200, 200);
        }

        if (isEnd) {
            //END SCREEN DOES NOT REACH ON CERTAIN SONGS (shunran, override(?))

            g2d.drawImage(load.getBg(), 0, 0, 1000, (int) (1000 * load.getBgRatio()),  null);
            g.setColor(Color.BLACK);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
            g2d.fillRect(0, 0, 1000, 600);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
            g2d.fillRect(25, 100, 250, 260);
            g2d.fillRect(500, 100, 400, 400);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

            //top bar
            g.fillRect(0, 0, 1000, 80);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString(load.getTitle(), 20, 25);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Beatmap by " + load.getMapper(), 20, 50);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Perfect: " + perf, 50, 140);
            g.drawString("Good: " + good, 50, 180);
            g.drawString("Miss: " + miss, 50, 220);
            g.drawString("Max Combo: " + maxCombo + "x", 50, 260);
            g.drawString("Accuracy: " + accuracy + "%", 50, 300);

            if (miss == 0) {
                g.setColor(Color.MAGENTA);
                g.drawString("FULL COMBO", 50, 340);
            }

            g.setColor(Color.BLACK);
            g2d.fill(back);
            g.setColor(Color.WHITE);
            g2d.draw(back);
            g.drawString("Back", 125, 532);



            g.setFont(new Font("Arial", Font.BOLD, 300));
            String score;
            if (accuracy == 100) {
                g.setColor(Color.decode("#dba400"));
                g.drawString("S", 620, 380);
                g.setColor(Color.ORANGE);
                score = "S";
            } else if (miss == 0 && accuracy >= 95) {
                g.setColor(Color.ORANGE);
                score = "S";
            } else if (accuracy >= 90) {
                g.setColor(Color.GREEN);
                score = "A";
            } else if (accuracy >= 80) {
                g.setColor(Color.BLUE);
                score = "B";
            } else if (accuracy >= 65) {
                g.setColor(Color.MAGENTA);
                score = "C";
            } else {
                g.setColor(Color.RED);
                score = "D";
            }
            g.drawString(score, 600, 400);
        }

//ADD SONG SELECTION
        if (isMenu) {
            audio.close();
            g.setColor(Color.MAGENTA);
            g2d.fill(play);
            g.setColor(Color.WHITE);
            g2d.draw(play);

            //FOR EACH FOLDER IN "Songs"
            //load.setMetaData(songName)
            //DISPLAY INFO AS BUTTONS(?)
            g.setColor(Color.BLACK);
            g.drawString("Selected: " + load.getTitle(), 400, 400);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Play", 800, 532);

            reset();
        }



    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (curTime * 1000 <= audio.getMicrosecondLength()) {
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
                    k2a = (float) (16f * 15);
//                System.out.println("K2");
                    hit(1);
                }
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
    public void mousePressed(MouseEvent e) {
        Point location = e.getPoint();
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (back.contains(location)) {
                isEnd = false;
                isMenu = true;
                repaint();
            } else if (play.contains(location)) {
                isMenu = false;
                isGame = true;
                reset();
                repaint();
            }

        }
    }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof Timer) {
            if (curTime < 0) {
                curTime += 10;
            } else {
                curTime = audio.getMicrosecondPosition() / 1000.0;
            }

            if (curTime >= 0 && !audio.isActive()) {
                audio.start();
            }

            if (curTime > endTime) {
                isEnd = true;
                isGame = false;
            }

            if (combo >= 50) {
                animCount++;
                if (animCount == 20) {
                    close = !close;
                    animCount = 0;
                }
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
        g.fillRect(0, 0, 1000, 100);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16)); //temp
        g.drawString("Perfect: " + perf, 50, 25);
        g.drawString("Good: " + good, 50, 50);
        g.drawString("Miss: " + miss, 50, 75);
        g.drawString("Accuracy: " + accuracy + "%", 200, 25);
        g.drawString("Max Combo: " + maxCombo + "x", 200, 50);
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

        drawFade(g2d, drumOut, k1a, 75, 125, -50, 100);
        drawFade(g2d, drumOut, k2a, 75, 125, 50, 100);
        drawFade(g2d, drumIn, d1a, 35, 135, 40, 80);
        drawFade(g2d, drumIn, d2a, 115, 135, -40, 80);

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

    private void drawFade(Graphics2D g2d, BufferedImage img, float alpha, int x, int y, int w, int h) {
        if (alpha > 1) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        } else {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        g2d.drawImage(img, x, y, w, h, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
    }

//CHANGE HIT WINDOWS
    private void hit(int keyColor) {
        double curHit = currentNote.getHitTime();
        int noteColor = currentNote.getColor();
        if (keyColor == noteColor) {
            if (curHit < curTime + 40 && curHit > curTime - 40) {
                perf++;
                combo++;
                song.removeFirst();
                if (!song.isEmpty()) {
                    currentNote = song.getFirst();
                }
            } else if (curHit < curTime + 80 && curHit > curTime - 80) {
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
            animCount = 0;
            close = true;
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

    private void reset() {
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
        close = true;

        curTime = -2000; //bus: 878000, override,shunran:-2000
        song = load.getSong(songName);
        endTime = song.getLast().getHitTime() + 3000;
        load.setMetaData(songName);
        loadMusic();
        audio.setMicrosecondPosition((long) (curTime * 1000));

        timer.start();
    }
}