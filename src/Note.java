public class Note {
    private int color;
    private double velocity;
    private boolean big;
    private double hitTime;
    private double xPos;

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
        return hitTime - 1000.0 / velocity;
    }

    public double getVelocity() {
        return velocity;
    }

//    public void setVelocity(double velocity) {
//        this.velocity = velocity;
//    }

    public double getxPos() {
        return xPos;
    }

    public int getColor() {
        return color;
    }

    //change mult
    //fix mult on high bpms
    public void move() {
        xPos -= velocity * .08;
    }

    @Override
    public String toString() {
        return hitTime + " " + color + " " + big + " " + velocity;
    }
}
