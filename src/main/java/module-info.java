module com.example.scenebuilderpractice {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.scenebuilderpractice to javafx.fxml;
    exports com.example.scenebuilderpractice;
}