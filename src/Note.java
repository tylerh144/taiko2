import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Note {
    private int color;
    private double velocity;
    private boolean big;
    private double hitTime;
    private double spawnTime;
    private double xPos;
    private BufferedImage img;
    private BufferedImage img1;

    public Note(double hitTime, int color, double velocity, double gameTick) {
        this.hitTime = hitTime;
        spawnTime = hitTime - 224000.0 / velocity;
        this.velocity = velocity * gameTick * .005 / 1.4;
        if (getSpawnTime() % gameTick != 0) {
            xPos = 1000 - (gameTick - (spawnTime % gameTick)) * velocity * 0.005 / 1.4 ;
        } else {
            xPos = 1000;
        }

        if (color == 0) {
            this.color = 0;
            big = false;
            try {
                img = ImageIO.read(new File("Assets/don.png"));
                img1 = ImageIO.read(new File("Assets/don1.png"));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else if (color == 8) {
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
                img = ImageIO.read(new File("Assets/don_b.png"));
                img1 = ImageIO.read(new File("Assets/don_b1.png"));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else if (color == 12) {
            this.color = 1;
            big = true;
            try {
                img = ImageIO.read(new File("Assets/ka_b.png"));
                img1 = ImageIO.read(new File("Assets/ka_b1.png"));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            //maybe remove spinners and sliders??????
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

    //fix time on high bpms
    public double getSpawnTime() {
        return spawnTime;
    }

    public double getVelocity() {
        return velocity;
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

    public BufferedImage getImg(boolean first) {
        if (first) {
            return img;
        } else {
            return img1;
        }
    }

    //change mult
    public void move() {
        xPos -= velocity;
    }

    @Override
    public String toString() {
        return hitTime + " " + color + " " + big + " " + velocity;
    }
}
