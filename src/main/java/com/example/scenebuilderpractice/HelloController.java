package com.example.scenebuilderpractice;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.control.ListView;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Media;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.FieldKey;



public class HelloController implements Initializable {
    @FXML
    private Label welcomeText;
    @FXML
    private Pane pane;
    @FXML
    private Label songName;
    @FXML
    private Label songArtist;
    @FXML
    private ImageView albumPhoto;
    @FXML
    private ListView<String> songList;
    @FXML
    private ToggleButton playButton, pauseButton, previousButton, nextButton;
    @FXML
    private ToggleButton slowedButton;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ProgressBar songProgressBar;

    private Media media;
    private MediaPlayer mediaPlayer;

    private File directory;
    private File[] files;

    private ArrayList<File> songs;

    private int songNumber;

    private Timer timer;
    private TimerTask task;
    private boolean running;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        songs = new ArrayList<File>();
        ObservableList<String> songNames = FXCollections.observableArrayList();

        directory = new File("Music");
        files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                songs.add(file);
                String fileName = file.getName();
                fileName = fileName.replaceFirst("[.][^.]+$", "");
                songNames.add(fileName);
                System.out.println(file);
            }
        }


        songList.setItems(songNames); // Set items to ListView

        songList.setStyle("-fx-background-color: #72555f; " +           // Dark background
                "-fx-control-inner-background: #72555f; " +     // Inner background
                "-fx-background: #72555f; ");


        // Allow user to select certain songs from the list
        songList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                songNumber = newValue.intValue();

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    if (running) {
                        stopTimer();
                    }
                }

                media = new Media(songs.get(songNumber).toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                songName.setText(songs.get(songNumber).getName());
                playMedia();
                songList.refresh();
            }
        });

        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        songName.setText(songs.get(songNumber).getName());
        slowedButton.setOnAction(this::slowMedia);
        volumeSlider.setValue(100); // Sets initial volume to 100
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {

                mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
            }
        });

    }

    public void setAlbumPhoto(File songFile) {
        try {
            AudioFile audioFile = AudioFileIO.read(songFile);
            Tag tag = audioFile.getTag();

            if (tag != null) {
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    Image image = new Image(new ByteArrayInputStream(imageData));
                    albumPhoto.setImage(image);
                } else {
                    System.out.println("No artwork found.");
                    albumPhoto.setImage(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            albumPhoto.setImage(null);
        }
    }

    public void playMedia() {
        setAlbumPhoto(songs.get(songNumber));
        songArtist.setText(getArtistName(songs.get(songNumber)));
        startTimer();
        mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
        slowedButton.setSelected(false);
        mediaPlayer.setRate(1.0); // Makes sure the slowedMedia rate change is turned off when the user selects new song
        mediaPlayer.play();
    }

    public void pauseMedia() {
        stopTimer();
        mediaPlayer.pause();
    }

    public void previousMedia() {
        if (songNumber > 0) {
            songNumber--;

            mediaPlayer.stop();

            if (running) {
                stopTimer();
            }

            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            songName.setText(songs.get(songNumber).getName());

            playMedia();
        } else {
            songNumber = songs.size() - 1;

            mediaPlayer.stop();

            if (running) {
                stopTimer();
            }

            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            songName.setText(songs.get(songNumber).getName());

            playMedia();
        }
    }

    public void nextMedia() {
        if (songNumber < songs.size() - 1) {
            songNumber++;

            mediaPlayer.stop();

            if (running) {
                stopTimer();
            }

            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            songName.setText(songs.get(songNumber).getName());

            playMedia();
        } else {
            songNumber = 0;

            mediaPlayer.stop();

            if (running) {
                stopTimer();
            }

            media = new Media(songs.get(songNumber).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            songName.setText(songs.get(songNumber).getName());

            playMedia();
        }
    }

    public void slowMedia(javafx.event.ActionEvent actionEvent) {
        ToggleButton slowedButton = (ToggleButton) actionEvent.getSource();

        if (slowedButton.isSelected()) {
            mediaPlayer.setRate(0.75); // Slow speed
        } else {
            mediaPlayer.setRate(1.0); // Normal speed
        }
    }

    public void startTimer() {
        timer = new Timer();
        task = new TimerTask() {

            @Override
            public void run() {
                running = true;
                double current = mediaPlayer.getCurrentTime().toSeconds();
                double end = media.getDuration().toSeconds();
                songProgressBar.setProgress(current / end);
                if (current / end == 1) {
                    stopTimer();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 50, 50);
        songProgressBar.setStyle("-fx-accent: #da00c3");
    }

    public void stopTimer() {
        running = false;
        timer.cancel();
    }

    private String getArtistName(File file) {
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            if (tag != null) {
                String artist = tag.getFirst(FieldKey.ARTIST);
                return artist.isEmpty() ? "Unknown Artist" : artist;
            }
        } catch (CannotReadException e) {
            throw new RuntimeException(e);
        } catch (TagException e) {
            throw new RuntimeException(e);
        } catch (InvalidAudioFrameException e) {
            throw new RuntimeException(e);
        } catch (ReadOnlyFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "Unknown Artist";
    }
}