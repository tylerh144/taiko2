import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DisplayPanel extends JPanel implements KeyListener, MouseListener, ActionListener, MouseWheelListener {
    private Timer timer;
    private double curTime, startTime, endTime, lastPauseTime;
    private boolean isMenu, isEnd, isGame, paused, hidden, easy, flashlight;
    private String activeMods, earlyLate;
    private boolean d1Down, d2Down, k1Down, k2Down;
    private ArrayList<Note> song;
    private Note currentNote;
    private int perf, good, early, late, miss, combo, maxCombo;
    private double accuracy, window;
    private boolean close;
    private int animCount;
    private Clip audio;
    private BufferedImage goodImg, missImg, drumIn, drumOut, spinnerCircle, spinnerHand;
    private int spinnerVelocity, spinnerAngle, lastHit;
    private double d1t, d2t, k1t, k2t, goodT, missT;
    private double missY;
    private Rectangle back, play, songArea, resume, toggleHD, toggleFL, toggleEZ;
    private ArrayList<Song> songList;
    private Song selectedSong;

    public DisplayPanel() {
        timer = new Timer(8, this);

        isMenu = true;
        isGame = false;
        isEnd = false;

        hidden = false;
        easy = false;
        flashlight = false;
        activeMods = "";

        back = new Rectangle(25, 500, 250, 50);
        play = new Rectangle(700, 500, 250, 50);
        resume = new Rectangle(375, 165, 250, 50);
        songArea = new Rectangle(530, 50, 470, 450);
        toggleHD = new Rectangle(25, 500, 50, 50);
        toggleFL = new Rectangle(85, 500, 50, 50);
        toggleEZ = new Rectangle(145, 500, 50, 50);
        songList = new ArrayList<>();

        File songFolder = new File("Songs");
        File[] songs = songFolder.listFiles();
        for (int i = 0; i < songs.length; i++) {
            Rectangle r = new Rectangle(530, 30 + 45*i, 460, 42);
            String path = songs[i].getName();
            String[] split = path.split("-");
            songList.add(new Song(path, r, split[0]));
        }

        selectedSong = songList.getFirst();

        try {
            goodImg = ImageIO.read(new File("Assets/good.png"));
            missImg = ImageIO.read(new File("Assets/miss.png"));
            drumIn = ImageIO.read(new File("Assets/taiko-drum-inner.png"));
            drumOut = ImageIO.read(new File("Assets/taiko-drum-outer.png"));
            spinnerCircle = ImageIO.read(new File("Assets/spinner-approachcircle.png"));
            spinnerHand = ImageIO.read(new File("Assets/spinner-circle.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        addMouseListener(this);
        addKeyListener(this);
        addMouseWheelListener(this);
        setFocusable(true);
        requestFocusInWindow();

        reset();
        audio.setMicrosecondPosition(selectedSong.getPreviewPoint() * 1000L);
        audio.setLoopPoints(audio.getFramePosition(), -1);
        audio.loop(Clip.LOOP_CONTINUOUSLY);
        audio.start();
        timer.start();
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
                        if (n.getHitTime() < curTime - 2.8*window && !(n instanceof Spinner)) {
                            song.remove(i);
                            miss();
                            i++;
                        } else if (curTime >= n.getSpawnTime()) {
                            n.move(curTime);

                            int x = (int) n.getxPos();

                            if (hidden && !(n instanceof Spinner)) {
                                float alpha = (float) ((x - 500) / 300.0);
                                if (alpha <= 0) {
                                    if (n.isKiai() && x > 333) {
                                        alpha = 0.01f;
                                    } else {
                                        alpha = 0;
                                    }
                                } else if (alpha >= 1) {
                                    alpha = 1;
                                }
                                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                            }

                            if (n.isBig()) {
                                g2d.drawImage(n.getImg(close), x - 19, 124, 106, 106, null);
                            } else {
                                g2d.drawImage(n.getImg(close), x, 143, 64, 64, null);
                            }
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                        }
                    }
                }
                if (currentNote instanceof Spinner s && curTime >= currentNote.getHitTime()) {
                    //spinner approach
                    int d = (int) (300 * (s.getEndTime() - curTime) / ((s.getEndTime()) - s.getHitTime()));
                    g2d.drawImage(spinnerCircle, (int) (400 - d / 2.0), (int) (250 - d / 2.0), d, d, null);

                    //spinner hand
                    //https://stackoverflow.com/a/64960185 (image rotation)
                    AffineTransform tr = new AffineTransform();
                    tr.translate(250, 100);
                    tr.rotate(Math.toRadians(spinnerAngle), 150, 150);
                    g2d.drawImage(spinnerHand, tr, null);

                    g.setFont(new Font("Arial", Font.BOLD, 32));
                    g.setColor(Color.WHITE);
                    g.drawString("" + s.getTicks(), 375, 400);

                    if (curTime > s.getEndTime()) {
                        miss();
                        s.resetTicks();
                        spinnerAngle = 0;
                        spinnerVelocity = 0;
                        newFirst();
                    }
                }
            }
            g.setColor(Color.BLACK);
            if (hidden) {
                g.fillRect(800, 100, 250, 152);
            }

            int flx = 500;
            if (combo >= 200) {
                flx = 400;
            } else if (combo >= 100) {
                flx = 450;
            }
            if (flashlight) {
                g.fillRect(flx, 100, 600, 152);
            }

            drawDrum(g);

            float goodA = (float) ((goodT + 400 - curTime) / 350);
            drawFade(g2d, goodImg, goodA, 132, 75, 200, 200);
            drawFade(g2d, missImg, (float) ((missT + 400 - curTime) / 100), 132, (int) missY, 200, 200);

            int x = 160;
            g.setColor(Color.RED);
            if (earlyLate.equals("Early")) {
                x = 260;
                g.setColor(Color.decode("#0091ff"));
            }
            if (goodA > .05) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
                g.fillRect(x-10, 100, 60, 30);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.drawString(earlyLate, x, 120);
            }

            if (curTime < startTime - 500) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 30));
                g.drawString("Skip", 900, 540);
            }

            if (paused) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .9f));
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, 1000, 600);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 30));
                g.drawString("Resume", 440, 200);
                g2d.draw(resume);
                g.drawString("Retry", 460, 300);
                g2d.draw(play);
                g.drawString("Quit", 470, 400);
                g2d.draw(back);
            }

        } else if (isEnd) {
            back.setLocation(25, 500);
            g2d.drawImage(selectedSong.getBg(), 0, 0, 1000, (int) (1000 * selectedSong.getBgRatio()),  null);
            g.setColor(Color.BLACK);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
            g2d.fillRect(0, 0, 1000, 600);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
            g2d.fillRect(25, 100, 250, 290);
            g2d.fillRect(500, 100, 400, 400);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

            //top bar
            g.fillRect(0, 0, 1000, 60);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString(selectedSong.getArtist() + " - " + selectedSong.getTitle(), 20, 25);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Beatmap by " + selectedSong.getMapper(), 20, 50);

            g.setColor(Color.RED);
            g.drawString("Late: " + late, 50, 220);
            g.setColor(Color.decode("#0091ff"));
            g.drawString("Early: " + early, 50, 200);

            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.setColor(Color.CYAN);
            g.drawString("Perfect: " + perf, 50, 140);
            g.setColor(Color.MAGENTA);
            g.drawString("Good: " + good, 50, 180);
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("Miss: " + miss, 50, 250);
            g.setColor(Color.WHITE);
            g.drawString("Max Combo: " + maxCombo + "x", 50, 290);
            g.drawString("Accuracy: " + accuracy + "%", 50, 330);


            if (perf == selectedSong.getChart().size()) {
                g.setColor(Color.CYAN);
                g.drawString("ALL PERFECT", 50, 370);
            } else if (miss == 0) {
                g.setColor(Color.MAGENTA);
                g.drawString("FULL COMBO", 50, 370);
            }

            g.setColor(Color.BLACK);
            g2d.fill(back);
            g.setColor(Color.WHITE);
            g2d.draw(back);
            g.drawString("Back", 125, 532);

            if (hidden) {
                g.setColor(Color.decode("#c7ac00"));
                g.fillRect(520, 430, 50, 50);
                g.setColor(Color.WHITE);
                g.drawRect(520, 430, 50, 50);
                g.drawString("HD", 529, 462);
            }
            if (flashlight) {
                g.setColor(Color.GRAY);
                g.fillRect(580, 430, 50, 50);
                g.setColor(Color.WHITE);
                g.drawRect(580, 430, 50, 50);
                g.drawString("FL", 590, 462);
            }
            if (easy) {
                g.setColor(Color.GREEN);
                g.fillRect(640, 430, 50, 50);
                g.setColor(Color.WHITE);
                g.drawRect(640, 430, 50, 50);
                g.drawString("EZ", 650, 462);
            }

            g.setFont(new Font("Arial", Font.BOLD, 300));
            String score;
            if (perf == selectedSong.getChart().size()) {
                if (hidden || flashlight) {
                    g.setColor(Color.GRAY);
                } else {
                    g.setColor(Color.decode("#dba400"));
                }
                g.drawString("S", 620, 380);
                if (hidden || flashlight) {
                    g.setColor(Color.LIGHT_GRAY);
                } else {
                    g.setColor(Color.ORANGE);
                }
                score = "S";
            } else if (miss == 0 && accuracy >= 95) {
                if (hidden || flashlight) {
                    g.setColor(Color.LIGHT_GRAY);
                } else {
                    g.setColor(Color.ORANGE);
                }
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

        } else if (isMenu) {
            play.setLocation(700, 500);
            //bg
            g2d.drawImage(selectedSong.getBg(), 0, 0, 1000, (int) (1000 * selectedSong.getBgRatio()),  null);
            g.setColor(Color.BLACK);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
            g2d.fillRect(0, 0, 1000, 600);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

            //song buttons
            for (int i = 0; i < songList.size(); i++) {
                Song s = songList.get(i);
                if (s == selectedSong) {
                    g.setColor(Color.decode("#349beb"));
                } else {
                    g.setColor(Color.decode("#f09030"));
                }
                Rectangle button = s.getButton();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .8f));
                g2d.fill(button);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
                g.setColor(Color.WHITE);
                g2d.draw(button);

                g.setFont(new Font("Arial Unicode MS", Font.BOLD, 16));
                g.drawString(s.getTitle(), 600, (int) (button.getY() + 20));
                g.drawString(s.getStarRating() + "★", 540, (int) (button.getY() + 28));
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                g.drawString(s.getArtist() + " // " + s.getMapper(), 600, (int) (button.getY() + 35));
            }

            //selected song bar
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 1000, 60);
            g.fillRect(0, 490, 1000, 110);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Selected: " + selectedSong.getArtist() + " - " + selectedSong.getTitle(), 10, 20);
            int time = (int) (selectedSong.getChart().getLast().getHitTime() / 1000);
            String minutes = "" + time / 60;
            if (minutes.length() == 1) {
                minutes = "0" + minutes;
            }
            String seconds = "" + time % 60;
            if (seconds.length() == 1) {
                seconds = "0" + seconds;
            }
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("Length: " + minutes + ":" + seconds + " BPM: " + Math.round(selectedSong.getBpm()) + " Objects: " + selectedSong.getChart().size(), 10, 38);

            g.setFont(new Font("Arial Unicode MS", Font.PLAIN, 10));
            double selOD = selectedSong.getOd();
            if (easy) {
                selOD /= 2;
                g.setColor(Color.GREEN);
            }
            g.drawString("OD: " + selOD + " Star Rating: " + selectedSong.getStarRating() + "★", 10, 52);


            g.setColor(Color.MAGENTA);
            g2d.fill(play);
            g.setColor(Color.WHITE);
            g2d.draw(play);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Play", 800, 532);

            activeMods = "";

            if (hidden) {
                g.setColor(Color.decode("#c7ac00"));
                if (activeMods.isEmpty()) {
                    activeMods = "Hidden";
                }
            } else {
                g.setColor(Color.WHITE);
            }
            g2d.draw(toggleHD);
            g.drawString("HD", 34, 532);

            if (flashlight) {
                g.setColor(Color.GRAY);
                if (activeMods.isEmpty()) {
                    activeMods = "Flashlight";
                } else {
                    activeMods += ", Flashlight";
                }
            } else {
                g.setColor(Color.WHITE);
            }
            g2d.draw(toggleFL);
            g.drawString("FL", 94, 532);

            if (easy) {
                g.setColor(Color.GREEN);
                if (activeMods.isEmpty()) {
                    activeMods = "Easy";
                } else {
                    activeMods += ", Easy";
                }
            } else {
                g.setColor(Color.WHITE);
            }
            g2d.draw(toggleEZ);
            g.drawString("EZ", 155, 532);

            g.setColor(Color.WHITE);
            g.drawString(activeMods, 25, 475);
        }



    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (isGame && !paused) {
            if (curTime <= audio.getMicrosecondLength() / 1000.0) {
                if (key == KeyEvent.VK_F) {
                    if (!d1Down) {
                        d1Down = true;
                        d1t = curTime;
                        hit(0);
                    }
                } else if (key == KeyEvent.VK_G) {
                    if (!d2Down) {
                        d2Down = true;
                        d2t = curTime;
                        hit(0);
                    }
                } else if (key == KeyEvent.VK_NUMPAD5) {
                    if (!k1Down) {
                        k1Down = true;
                        k1t = curTime;
                        hit(1);
                    }
                } else if (key == KeyEvent.VK_NUMPAD6) {
                    if (!k2Down) {
                        k2Down = true;
                        k2t = curTime;
                        hit(1);
                    }
                } else if (key == KeyEvent.VK_ESCAPE) {
                    if (curTime >= lastPauseTime + 1000) {
                        paused = true;
                        lastPauseTime = curTime;
                        audio.stop();
                        back.setLocation(375, 365);
                        play.setLocation(375, 265);
                    }
                } else if (key == KeyEvent.VK_SPACE && curTime < startTime - 500) {
                    curTime = startTime-500;
                    audio.setMicrosecondPosition((long) (curTime * 1000));
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
            if (back.contains(location) && (isEnd || paused)) {
                isEnd = false;
                isGame = false;
                isMenu = true;
                audio.close();

                reset();
                audio.setMicrosecondPosition(selectedSong.getPreviewPoint() * 1000L);
                audio.setLoopPoints(audio.getFramePosition(), -1);
                audio.loop(Clip.LOOP_CONTINUOUSLY);
                audio.start();

                repaint();
            } else if (play.contains(location) && (isMenu || paused)) {
                isMenu = false;
                isGame = true;
                isEnd = false;
                reset();
                repaint();
            } else if (isMenu) {
                for (Song s : songList) {
                    if (s.getButton().contains(location) && songArea.contains(location)) {
                        if (s == selectedSong) {
                            isMenu = false;
                            isGame = true;
                            isEnd = false;
                            reset();
                        } else {
                            selectedSong = s;
                            audio.stop();
                            audio.close();
                            loadMusic();
                            audio.setMicrosecondPosition(selectedSong.getPreviewPoint() * 1000L);
                            audio.setLoopPoints(audio.getFramePosition(), -1);
                            audio.loop(Clip.LOOP_CONTINUOUSLY);
                            audio.start();
                        }
                        repaint();
                    }
                }
                if (toggleHD.contains(location)) {
                    hidden = !hidden;
                }
                if (toggleEZ.contains(location)) {
                    easy = !easy;
                }
                if (toggleFL.contains(location)) {
                    flashlight = !flashlight;
                }
            } else if (resume.contains(location) && paused) {
                paused = false;
                if (curTime >= 0) {
                    audio.start();
                }
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
    public void mouseWheelMoved(MouseWheelEvent e) {
        Point location = e.getPoint();
        int dir = e.getWheelRotation();

        if (songArea.contains(location) && isMenu) {
            double firstY = songList.getFirst().getButton().getY();
            double lastY = songList.getLast().getButton().getY();

            if (firstY < 100 && dir == -1) {
                for (Song s : songList) {
                    s.getButton().setLocation(530, (int) (s.getButton().getY() + 20));
                }
            } else if (lastY > 400 && dir == 1) {
                for (Song s : songList) {
                    s.getButton().setLocation(530, (int) (s.getButton().getY() - 20));
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof Timer) {
            if (isGame && !paused) {
                if (!audio.isActive()) {
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
                    if (animCount > 5000 / selectedSong.getBpm()) {
                        close = !close;
                        animCount = 0;
                    }
                }

                missY += .5;

                if (currentNote instanceof Spinner) {
                    spinnerAngle += spinnerVelocity;
                    if (spinnerVelocity > 0) {
                        spinnerVelocity -= 3;
                    }
                }
            }
            repaint();
        }
    }

    private void drawLane(Graphics g) {
        //bg img
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(selectedSong.getBg(), 0, 120 + selectedSong.getyOffset(), 1000, (int) (1000 * selectedSong.getBgRatio()),  null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .8f));
        g.setColor(Color.BLACK);
        g2d.fillRect(0, 250, 1000, 325);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

        //note lane
        g.setColor(Color.decode("#111111"));
        g.fillRect(0, 100, 1000, 150);
        g2d.setStroke(new BasicStroke(5));
        g.setColor(Color.BLACK);
        g.drawRect(0, 100, 1000, 150);
        g2d.setStroke(new BasicStroke(1));
        if (currentNote.isKiai()) {
            g.setColor(Color.CYAN);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f));
            g.drawRect(0, 100, 1000, 150);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        }

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
        g.drawString("Current Time: " + (int) curTime, 200, 75);
        if (!activeMods.isEmpty()) {
            g.drawString("Active Mods: " + activeMods, 400, 25);
        }

        if (curTime > startTime) {
            g.setColor(Color.LIGHT_GRAY);
            g.fillArc(900, 25, 50, 50, 90, (int) ((curTime-startTime)/(endTime-startTime) * -360));
        } else {
            g.setColor(Color.decode("#70c416"));
            g.fillArc(900, 25, 50, 50, 90, (int) (((curTime-startTime)/(-2000-startTime))  * 360));
        }
        g.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g.drawOval(900, 25, 50, 50);
        g.drawOval(924, 49, 2, 2);
        g2d.setStroke(new BasicStroke(1));

        //hit indicator
        g.setColor(Color.WHITE);
        g.drawOval(192, 135, 80, 80);

        g.setColor(Color.DARK_GRAY);
        g.fillOval(200, 143, 64, 64);
    }

    private void drawDrum(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.BLACK);
        g.fillRect(0, 100, 150, 152);

        g.setColor(Color.DARK_GRAY);
        g.fillOval(25, 125, 100, 100);

        drawFade(g2d, drumOut, (float) ((k1t + 100 - curTime) / 10), 75, 125, -50, 100);
        drawFade(g2d, drumOut, (float) ((k2t + 100 - curTime) / 10), 75, 125, 50, 100);
        drawFade(g2d, drumIn, (float) ((d1t + 100 - curTime) / 10), 35, 135, 40, 80);
        drawFade(g2d, drumIn, (float) ((d2t + 100 - curTime) / 10), 115, 135, -40, 80);

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
        } else if (alpha < 0) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0));
        } else {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        g2d.drawImage(img, x, y, w, h, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
    }

    private void hit(int keyColor) {
        if (!song.isEmpty() && curTime > 0) {
            double curHit = currentNote.getHitTime();
            int noteColor = currentNote.getColor();
            if (currentNote instanceof Spinner curSpinner) {
                if (curTime >= curHit) {
                    if (keyColor != lastHit || curSpinner.getTicks() == curSpinner.getMaxTicks()) {
                        curSpinner.tickDown();
                        spinnerVelocity = 60;
                        lastHit = keyColor;
                        if (curSpinner.getTicks() <= 0) {
                            curSpinner.resetTicks();
                            perf++;
                            combo++;
                            if (combo > maxCombo) {
                                maxCombo = combo;
                            }
                            newFirst();
                        }
                    }
                }
            } else {
                if (keyColor == noteColor) {
                    if (curHit < curTime + window && curHit > curTime - window) {
                        perf++;
                        combo++;
                        newFirst();
                    } else if (curHit < curTime + 2*window && curHit > curTime) {
                        good++;
                        combo++;
                        goodT = curTime;
                        earlyLate = "Early";
                        early++;
                        newFirst();
                    } else if (curHit > curTime - 2*window && curHit < curTime) {
                        good++;
                        combo++;
                        goodT = curTime;
                        earlyLate = "Late";
                        late++;
                        newFirst();
                    }
                } else if (curHit < curTime + 2.8*window && curHit > curTime - 2.8*window) {
                    miss();
                    newFirst();
                }
                if (combo > maxCombo) {
                    maxCombo = combo;
                }
            }
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

    private void miss() {
        miss++;
        animCount = 0;
        close = true;
        missT = curTime;
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
    }

    private void newFirst() {
        song.removeFirst();
        if (!song.isEmpty()) {
            currentNote = song.getFirst();
        }
    }

    private void calcAcc() {
        double sumHit = perf + good * 2/3.0;
        double sumAll = perf + good + miss;
        accuracy = sumHit / sumAll * 10000;
        accuracy = Math.round(accuracy) / 100.0;
    }

    private void loadMusic() {
        File audioFile = new File(selectedSong.getAudioPath());
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

        spinnerVelocity = 0;
        spinnerAngle = 0;

        perf = 0;
        good = 0;
        early = 0;
        late = 0;
        miss = 0;
        combo = 0;
        maxCombo = 0;
        accuracy = 0;

        animCount = 0;
        close = true;
        earlyLate = "";

        song = new ArrayList<>();
        song.addAll(selectedSong.getChart());
        startTime = song.getFirst().getSpawnTime();
        if (song.getLast() instanceof Spinner s) {
            endTime = s.getEndTime() + 2000;
        } else {
            endTime = song.getLast().getHitTime() + 2000;
        }
        currentNote = song.getFirst();
        curTime = -2000;
        paused = false;
        lastPauseTime = -10000;

        d1t = curTime-2000;
        d2t = curTime-2000;
        k1t = curTime-2000;
        k2t = curTime-2000;
        goodT = curTime-2000;
        missT = curTime-2000;

        if (easy) {
            window = 54 - 1.5 * selectedSong.getOd();
        } else {
            window = 54 - 3 * selectedSong.getOd();
        }

        if (audio != null && audio.isOpen()) {
            audio.stop();
            audio.close();
        }
        loadMusic();
        audio.setMicrosecondPosition((long) (curTime * 1000));
    }
}