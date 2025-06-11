import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

public class Song {
    private BufferedImage bg;
    private double bgRatio;
    private int yOffset;
    private String title, artist, mapper, audioPath, starRating;
    private ArrayList<Note> chart;
    private Rectangle button;
    private int previewPoint;
    private double od;

    public Song(String path, Rectangle r, String sr) {
        parseData(path);
        audioPath = "Songs/" + path + "/audio.wav";
        button = r;
        starRating = sr;
        try {
            bg = ImageIO.read(new File("Songs/" + path + "/bg.jpg"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        bgRatio = (double) bg.getHeight() / bg.getWidth();
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

    public String getStarRating() {
        return starRating;
    }

    public double getOd() {
        return od;
    }

    public double getBgRatio() {
        return bgRatio;
    }

    public int getyOffset() {
        return yOffset;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public int getPreviewPoint() {
        return previewPoint;
    }

    public Rectangle getButton() {
        return button;
    }

    public ArrayList<Note> getChart() {
        return chart;
    }

    private void parseData(String path) {
        chart = new ArrayList<>();
        ArrayList<Double[]> timeVelocity = new ArrayList<>();

        try {
            File myFile = new File("Songs/" + path + "/data.osu");
            Scanner fileScanner = new Scanner(myFile);

            while (!fileScanner.nextLine().equals("[General]")) {
                //skip till preview
            }
            fileScanner.nextLine();
            fileScanner.nextLine();
            String str = fileScanner.nextLine();
            String[] split = str.split(": ");
            previewPoint = Integer.parseInt(split[1]);

            while (!fileScanner.nextLine().equals("[Metadata]")) {
                //skip until metadata
            }
            str = fileScanner.nextLine();
            split = str.split(":");
            title = split[1];
            fileScanner.nextLine();

            str = fileScanner.nextLine();
            split = str.split(":");
            artist = split[1];
            fileScanner.nextLine();

            str = fileScanner.nextLine();
            split = str.split(":");
            mapper = split[1];

            while (!fileScanner.nextLine().equals("[Difficulty]")) {
                //skip until sliderMult
            }
            fileScanner.nextLine();
            fileScanner.nextLine();

            str = fileScanner.nextLine();
            split = str.split(":");
            od = Double.parseDouble(split[1]);
            fileScanner.nextLine();


            str = fileScanner.nextLine();
            split = str.split(":");
            double sliderMult = Double.parseDouble(split[1]);

            while (!fileScanner.nextLine().equals("[Events]")) {
                //skip until bg offset
            }
            fileScanner.nextLine();
            str = fileScanner.nextLine();
            split = str.split(",");
            if (split.length == 3) {
                str = fileScanner.nextLine();
                split = str.split(",");
            }
            yOffset = Integer.parseInt(split[4]);

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
                    double kiai = Double.parseDouble(splitData[7]);

                    timeVelocity.add(new Double[]{hitTime, velocity, kiai});
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
                boolean kiai = false;
                for (int i = timeVelocity.size()-1; i >= 0; i--) {
                    Double[] arr = timeVelocity.get(i);
                    if (hitTime >= arr[0]) {
                        velocity = arr[1];
                        if (arr[2] == 1) {
                            kiai = true;
                        }
                        break;
                    }
                }
//                for (Double[] array : timeVelocity) {
//                    if (hitTime >= array[0]) {
//                        velocity = array[1];
//                    }
//                }
                if (splitData.length == 6) {
                    chart.add(new Note(hitTime, color, velocity, kiai));
                } else {
                    chart.add(new Spinner(hitTime, Integer.parseInt(splitData[5]), velocity, kiai));
                }
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
