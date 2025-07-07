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

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;



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
    private Button importButton;
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
    private ObservableList<String> songNames = FXCollections.observableArrayList();

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        songs = new ArrayList<>();
        songList.setItems(songNames);

        songList.setStyle("-fx-background-color: #72555f; " +           // Dark background
                "-fx-control-inner-background: #72555f; " +     // Inner background
                "-fx-background: #72555f; ");


        // Allow user to select certain songs from the list
        songList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                int selectedIndex = newValue.intValue();
                if (selectedIndex >= 0 && selectedIndex < songs.size()) {
                    songNumber = selectedIndex;
                    playMedia();
                }

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

        songName.setText("No song selected");
        songArtist.setText("");
        slowedButton.setOnAction(this::slowMedia);
        volumeSlider.setValue(100); // Sets initial volume to 100
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
            }
        });

    }

    public void importMedia() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Files");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("mp3 files", "*.mp3")
        );

        Stage stage = (Stage) importButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            songs.add(file);
            String cleanName = file.getName().replaceFirst("[.][^.]+$", "");
            songNames.add(cleanName);
            System.out.println("Imported: " + file.getAbsolutePath());
        }
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
        if (songs.isEmpty()) {
            System.out.println("No songs to play.");
            return;
        }

        File currentSong = songs.get(songNumber);

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            if (running) {
                stopTimer();
            }
        }

        setAlbumPhoto(songs.get(songNumber));
        songArtist.setText(getArtistName(songs.get(songNumber)));
        songName.setText(currentSong.getName());
        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
        slowedButton.setSelected(false);
        mediaPlayer.setRate(1.0); // Makes sure the slowedMedia rate change is turned off when the user selects new song
        mediaPlayer.play();

        startTimer();
        songList.refresh();
    }

    public void pauseMedia() {
        stopTimer();
        mediaPlayer.pause();
    }

    public void previousMedia() {
        if(songs.isEmpty()) {
            System.out.println("No songs loaded");
            return;
        }
        songNumber = (songNumber > 0) ? songNumber - 1 : songs.size() - 1;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            if (running) {
                stopTimer();
            }
        }
        playMedia();
    }

    public void nextMedia() {
        if(songs.isEmpty()) {
            System.out.println("No songs loaded");
            return;
        }
        songNumber++; // Move to next song

        // Wrap around to the beginning if we reach the end
        if (songNumber >= songs.size()) {
            songNumber = 0;
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            if (running) {
                stopTimer();
            }
        }
        playMedia();
    }

    public void slowMedia(javafx.event.ActionEvent actionEvent) {
        ToggleButton slowedButton = (ToggleButton) actionEvent.getSource();

        if (slowedButton.isSelected()) {
            mediaPlayer.setRate(0.85); // Slow speed
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