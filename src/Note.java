public class Note {
    private int color;
    private double velocity;
    private boolean big;
    private double hitTime;
    private int xPos;

    public Note(double hitTime, int color, double velocity) {
        this.hitTime = hitTime;
        this.velocity = velocity;
        xPos = 1000;
        if (color == 0) {
            this.color = 0;
            big = false;
        } else if (color == 8) {
            this.color = 1;
            big = false;
        } else if (color == 4) {
            this.color = 0;
            big = true;
        } else if (color == 12) {
            this.color = 1;
            big = true;
        }
    }

    public double getHitTime() {
        return hitTime;
    }

    public double getSpawnTime() {
        return (int) (hitTime - 100 / (120 * velocity));
    }

    public double getVelocity() {
        return velocity;
    }

//    public void setVelocity(double velocity) {
//        this.velocity = velocity;
//    }

    public int getxPos() {
        return xPos;
    }

    public int getColor() {
        return color;
    }

    //change mult
    public void move() {
        xPos -= (int) (velocity * .1);
    }

    @Override
    public String toString() {
        return hitTime + " " + color + " " + big + " " + velocity;
    }
}
