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

    //get image
    //get audio

    private void parseData(String songName) {
        song = new ArrayList<>();
        timeVelocity = new ArrayList<>();
        try {
            File myFile = new File("Songs/" + songName + "/data.osu");
            Scanner fileScanner = new Scanner(myFile);

            while (!fileScanner.nextLine().equals("[TimingPoints]")) {
                //skip until timingpoints
            }

            boolean hit = false;

            while (!hit) {
                String data = fileScanner.nextLine();
                if (data.isEmpty()) {
                    hit = true;
                } else {
                    String[] splitData = data.split(",");
                    double hitTime = Double.parseDouble(splitData[0]);
                    double velocityMult = Double.parseDouble(splitData[1]);
                    double velocity;
                    if (velocityMult > 0) {
                        velocity = 60000 / velocityMult;
                    } else {
                        velocity = timeVelocity.getFirst()[1] * (-100 / velocityMult);
                    }

                    timeVelocity.add(new Double[]{hitTime, velocity});
                }
            }

            fileScanner.nextLine();
            fileScanner.nextLine();

            while (fileScanner.hasNext()) {
                String data = fileScanner.nextLine();
                String[] splitData = data.split(",");
                double hitTime = Double.parseDouble(splitData[2]);
                int color = Integer.parseInt(splitData[4]);
                double velocity = 0;
                for (Double[] array : timeVelocity) {
                    if (hitTime >= array[0]) {
                        velocity = array[1];
                    }
                }
                Note n = new Note(hitTime, color, velocity);
                song.add(n);
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

}
