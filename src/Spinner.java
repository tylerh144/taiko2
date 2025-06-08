public class Spinner extends Note {
    private double endTime;
    private int ticks;
    private final int TICKS;

    public Spinner(double hitTime, int spinnerEndTime, double velocity) {
        super(hitTime, spinnerEndTime, velocity);
        this.endTime = spinnerEndTime;
        TICKS = (int) ((spinnerEndTime - hitTime) / 120);
        ticks = TICKS;
    }

    public void tickDown() {
        ticks--;
    }

    public void resetTicks() {
        ticks = TICKS;
    }

    public int getTicks() {
        return ticks;
    }

    public int getMaxTicks() {
        return TICKS;
    }

    public double getEndTime() {
        return endTime;
    }
}
