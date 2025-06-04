import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Song {
    private BufferedImage bg;
    private double bgRatio;
    private String title, artist, mapper, audioPath;
    private ArrayList<Note> chart;
    private Rectangle button;
    private int previewPoint;

    public Song(BufferedImage bg, double bgRatio, String title, String artist, String mapper, String audioPath, ArrayList<Note> chart, Rectangle button) {
        this.bg = bg;
        this.bgRatio = bgRatio;
        this.title = title;
        this.artist = artist;
        this.mapper = mapper;
        this.audioPath = audioPath;
        this.chart = chart;
        this.button = button;
    }

    public BufferedImage getBg() {
        return bg;
    }

    public double getBgRatio() {
        return bgRatio;
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

    public String getAudioPath() {
        return audioPath;
    }

    public ArrayList<Note> getChart() {
        return chart;
    }

    public Rectangle getButton() {
        return button;
    }
}
