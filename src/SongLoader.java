import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

public class SongLoader {
    private BufferedImage bg;
    private String audioPath;
    private String title, artist, mapper;

    public SongLoader () {
    }

    public ArrayList<Note> getSong(String songName) {
        ArrayList<Note> song = new ArrayList<>();
        ArrayList<Double[]> timeVelocity = new ArrayList<>();
        try {
            File myFile = new File("Songs/" + songName + "/data.osu");
            Scanner fileScanner = new Scanner(myFile);

            //MAYBE ADD OVERALL DIFFICULTY
            while (!fileScanner.nextLine().equals("[Difficulty]")) {
                //skip until sliderMult
            }
            for (int i = 0; i < 4; i++) {
                fileScanner.nextLine();
            }
            String str = fileScanner.nextLine();
            String[] split = str.split(":");

            double sliderMult = Double.parseDouble(split[1]);

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
                        velocity = (60000 / velocityMult) * sliderMult;
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
                if (splitData.length == 7) {
                    color = Integer.parseInt(splitData[5]);
                }
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
        return song;
    }

    public void setMetaData(String songName) {
        audioPath = "Songs/" + songName + "/audio.wav";
        try {
            bg = ImageIO.read(new File("Songs/" + songName + "/bg.jpg"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        parseMetaData(songName);
    }

    public BufferedImage getBg() {
        return bg;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getMapper() {
        return mapper;
    }

    public double getBgRatio() {
        return (double) bg.getHeight() / bg.getWidth();
    }

    public String getAudioPath() {
        return audioPath;
    }

    private void parseMetaData(String songName) {
        try {
            File myFile = new File("Songs/" + songName + "/data.osu");
            Scanner fileScanner = new Scanner(myFile);
        while (!fileScanner.nextLine().equals("[Metadata]")) {
            //skip until metadata
        }
        String str = fileScanner.nextLine();
        String[] split = str.split(":");
        title = split[1];
        fileScanner.nextLine();

        str = fileScanner.nextLine();
        split = str.split(":");
        artist = split[1];
        fileScanner.nextLine();

        str = fileScanner.nextLine();
        split = str.split(":");
        mapper = split[1];
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

}
