public class Main {
    public static void main(String[] args) {
        SongLoader test = new SongLoader();
        for (Note n : test.getSong()) {
            System.out.println(n);
        }
    }
}
