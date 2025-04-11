public class Main {
    public static void main(String[] args) {
        SongLoader test = new SongLoader();
        for (Note n : test.getSong("bus")) {
            System.out.println(n);
        }
    }
}
