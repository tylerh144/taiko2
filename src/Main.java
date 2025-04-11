public class Main {
    public static void main(String[] args) {
        SongLoader test = new SongLoader();
        for (Note n : test.getSong("DNA")) {
            System.out.println(n);
        }
        SampleFrame frame = new SampleFrame();
    }
}
