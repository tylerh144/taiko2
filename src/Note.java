public class Note {
    private int color;
    private double velocity;
    private boolean big;
    private double hitTime;
    private double spawnTime;

    public Note(double hitTime, int color, double velocity) {
        this.hitTime = hitTime;
        this.velocity = velocity;
        this.spawnTime = 0;
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

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
        spawnTime = 0;
    }

    @Override
    public String toString() {
        return hitTime + " " + color + " " + big + " " + velocity;
    }
}
