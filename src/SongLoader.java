import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

public class SongLoader {
    private ArrayList<Note> song;
    private ArrayList<Double[]> timeVelocity;

    public SongLoader () {
        song = new ArrayList<>();
        timeVelocity = new ArrayList<>();
    }

    public ArrayList<Note> getSong(String songName) {
        parseData(songName);
        return song;
    }

    private void parseData(String songName) {
        song = new ArrayList<>();
        timeVelocity = new ArrayList<>();
        try {
            File myFile = new File("Songs/" + songName + "/hitobjects.txt");
            Scanner fileScanner = new Scanner(myFile);
            while (fileScanner.hasNext()) {
                String data = fileScanner.nextLine();
                String[] splitData = data.split(",");
                double hitTime = Double.parseDouble(splitData[2]);
                int color = Integer.parseInt(splitData[4]);
                Note n = new Note(hitTime, color);
                song.add(n);
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }

        try {
            File myFile = new File("Songs/" + songName + "/timingpoints.txt");
            Scanner fileScanner = new Scanner(myFile);
            while (fileScanner.hasNext()) {
                String data = fileScanner.nextLine();
                String[] splitData = data.split(",");
                double hitTime = Double.parseDouble(splitData[0]);
                double velocityMult = Double.parseDouble(splitData[1]);
                double velocity = 0;
                if (velocityMult > 0) {
                    velocity = 60000/velocityMult;
                } else {
                    velocity = timeVelocity.getFirst()[1] * (-100 / velocityMult);
                }

                timeVelocity.add(new Double[]{hitTime, velocity});
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }

        for (Double[] array : timeVelocity) {
            for (Note n : song) {
                if (n.getHitTime() >= array[0]) {
                    n.setVelocity(array[1]);
                }
            }
        }
    }

}
