import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

public class Song extends Thread {
    private BufferedImage bg;
    private double bgRatio;
    private int yOffset;
    private String title, artist, mapper, audioPath, starRating;
    private ArrayList<Note> chart;
    private Rectangle button;
    private int previewPoint;
    private double od;
    private String path;

    public Song(String path, Rectangle r, String sr) {
        this.path = path;
        audioPath = "Songs/" + path + "/audio.wav";
        button = r;
        starRating = sr;
        try {
            bg = ImageIO.read(new File("Songs/" + path + "/bg.jpg"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        bgRatio = (double) bg.getHeight() / bg.getWidth();
        start();
    }

    public void run() {
        try {
            parseData(path);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
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
            BufferedReader br = new BufferedReader(new FileReader("Songs/" + path + "/data.osu"));

            while (!br.readLine().equals("[General]")) {
                //skip till preview
            }
            br.readLine();
            br.readLine();
            String str = br.readLine();
            String[] split = str.split(": ");
            previewPoint = Integer.parseInt(split[1]);

            while (!br.readLine().equals("[Metadata]")) {
                //skip until metadata
            }
            str = br.readLine();
            split = str.split(":");
            title = split[1];
            br.readLine();

            str = br.readLine();
            split = str.split(":");
            artist = split[1];
            br.readLine();

            str = br.readLine();
            split = str.split(":");
            mapper = split[1];

            while (!br.readLine().equals("[Difficulty]")) {
                //skip until sliderMult
            }
            br.readLine();
            br.readLine();

            str = br.readLine();
            split = str.split(":");
            od = Double.parseDouble(split[1]);
            br.readLine();


            str = br.readLine();
            split = str.split(":");
            double sliderMult = Double.parseDouble(split[1]);

            while (!br.readLine().equals("[Events]")) {
                //skip until bg offset
            }
            br.readLine();
            str = br.readLine();
            split = str.split(",");
            if (split.length == 3) {
                str = br.readLine();
                split = str.split(",");
            }
            yOffset = Integer.parseInt(split[4]);

            while (!br.readLine().equals("[TimingPoints]")) {
                //skip until timingpoints
            }

            boolean hit = false;

            while (!hit) {
                String data = br.readLine();
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

            br.readLine();
            br.readLine();

            String data;
            while ((data = br.readLine()) != null) {
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

//                for (int i = 1; i < timeVelocity.size(); i++) {
//                    Double[] before = timeVelocity.get(i-1);
//                    Double[] after = timeVelocity.get(i);
//                    if (hitTime < after[0]) {
//                        velocity = before[1];
//                        if (before[2] == 1) {
//                            kiai = true;
//                        }
//                        break;
//                    } else {
//                        timeVelocity.remove(i-1);
//                        i--;
//                    }
//                }

                if (splitData.length == 6) {
                    chart.add(new Note(hitTime, color, velocity, kiai));
                } else {
                    chart.add(new Spinner(hitTime, Integer.parseInt(splitData[5]), velocity, kiai));
                }
            }
            br.close();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
