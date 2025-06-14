import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Note {
    private int color;
    private double velocity;
    private boolean big, kiai;
    private double hitTime, spawnTime;
    private double xPos;
    private BufferedImage img, img1;

    public Note(double hitTime, int color, double velocity, boolean kiai) {
        this.velocity = velocity * .004 / 1.4;
        this.hitTime = hitTime;
        spawnTime = hitTime - 800 / this.velocity;
        this.kiai = kiai;

        if (color == 0) {
            this.color = 0;
            big = false;
            try {
                img = ImageIO.read(new File("Assets/don.png"));
                img1 = ImageIO.read(new File("Assets/don1.png"));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else if (color == 8 || color == 2) {
            this.color = 1;
            big = false;
            try {
                img = ImageIO.read(new File("Assets/ka.png"));
                img1 = ImageIO.read(new File("Assets/ka1.png"));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else if (color == 4) {
            this.color = 0;
            big = true;
            try {
                img = ImageIO.read(new File("Assets/don_b1.png"));
                img1 = ImageIO.read(new File("Assets/don_b.png"));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else if (color == 12 || color == 6) {
            this.color = 1;
            big = true;
            try {
                img = ImageIO.read(new File("Assets/ka_b1.png"));
                img1 = ImageIO.read(new File("Assets/ka_b.png"));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            //spinner super
            this.color = -1;
            big = true;
            //color = endhittime
            try {
                img = ImageIO.read(new File("Assets/spinner-warning.png"));
                img1 = img;
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    public double getHitTime() {
        return hitTime;
    }

    public double getSpawnTime() {
        return spawnTime;
    }

    public double getxPos() {
        return xPos;
    }

    public int getColor() {
        return color;
    }

    public boolean isBig() {
        return big;
    }

    public boolean isKiai() {
        return kiai;
    }

    public BufferedImage getImg(boolean first) {
        if (first) {
            return img;
        } else {
            return img1;
        }
    }

    public void move(double curTime) {
        xPos = 1000 - (curTime - spawnTime) * velocity;
    }

    @Override
    public String toString() {
        return hitTime + " " + color + " " + big + " " + velocity;
    }
}
