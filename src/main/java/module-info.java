module com.example.scenebuilderpractice {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;
    requires jaudiotagger;


    opens com.example.scenebuilderpractice to javafx.fxml;
    exports com.example.scenebuilderpractice;
}