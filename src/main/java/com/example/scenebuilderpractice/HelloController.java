package com.example.scenebuilderpractice;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Pane;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;



public class HelloController implements Initializable {
    @FXML
    private Pane pane;
    @FXML
    private Label runnersNameLabel;
    @FXML
    private TextField nameField;
    @FXML
    private Label runnersDistanceLabel;
    @FXML
    private TextField distanceField;
    @FXML
    private Label timeLabel;
    @FXML
    private TextField timeField;
    @FXML
    private Button addRunToList;
    @FXML
    private RadioButton nameRadio, distanceRadio, timeRadio, paceRadio;
    @FXML
    private CheckBox descendingBox;
    @FXML
    private Button listToCsvButton;
    @FXML
    private TableView<Run> tableView;
    @FXML
    private TableColumn<Run, String> nameCol;
    @FXML
    private TableColumn<Run, String> distCol;
    @FXML
    private TableColumn<Run, String> timeCol;
    @FXML
    private TableColumn<Run, String> paceCol;

    private final RunStore store = new RunStore();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tableView.setItems(store.getView());

        ToggleGroup group = new ToggleGroup();
        nameRadio.setToggleGroup(group);
        distanceRadio.setToggleGroup(group);
        timeRadio.setToggleGroup(group);
        paceRadio.setToggleGroup(group);

        // Columns with formatted presentation
        nameCol.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getName));
        distCol.setCellValueFactory(cd -> Bindings.createStringBinding(() -> Run.formatMiles(cd.getValue().distanceInMiles)));
        timeCol.setCellValueFactory(cd -> Bindings.createStringBinding(cd.getValue()::getTimeFormatted));
        paceCol.setCellValueFactory(cd ->Bindings.createStringBinding(cd.getValue()::getPaceFormatted));
        nameRadio.setSelected(true);
        applySort();
    }
    @FXML
    private void onAdd() {
        try {
            String name = nameField.getText();
            double miles = Double.parseDouble(distanceField.getText());
            double mins = parseTimeToMinutes(timeField.getText());

            Run run = new Run(name, miles, mins);
            store.add(run);

            nameField.clear();
            distanceField.clear();
            timeField.clear();
        } catch (Exception e) {
            alert("Invalid input", e.getMessage());
        }
    }

    @FXML
    private void onSort() {
        applySort();
    }

    // ASC & DESC formatting
    private void applySort() {
        boolean asc = !descendingBox.isSelected();
        if (nameRadio.isSelected()) {
            store.setComparator(Run.BY_NAME, asc);
        } else if (distanceRadio.isSelected()) {
            store.setComparator(Run.BY_DISTANCE_DESC, asc);
        } else if (timeRadio.isSelected()) {
            store.setComparator(Run.BY_TIME_ASC, asc);
        } else if (paceRadio.isSelected()) {
            store.setComparator(Run.BY_PACE_ASC, asc);
        }
    }

    @FXML
    private void onSave() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Save runs as CSV");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.txt", "*.csv"));
            fc.setInitialFileName("FinalProjectRunners.txt");

            File file = fc.showSaveDialog(tableView.getScene().getWindow());
            if (file == null) return;

            store.exportCsv(Path.of(file.toURI()));
        } catch (Exception ex) {
            alert("Save failed", ex.getMessage());
        }
    }

    private static double parseTimeToMinutes(String text) {
        String t = text.trim();
        if (t.isEmpty()) throw new IllegalArgumentException("Time Required");
        // Accept h:mm:ss or mm:ss or m
        String[] sections = t.split(":");
        long h = 0, m, s = 0;

        if (sections.length == 3) {
            h = Long.parseLong(sections[0]);
            m = Long.parseLong(sections[1]);
            s = Long.parseLong(sections[2]);
        } else if (sections.length == 2) {
            m = Long.parseLong(sections[0]);
            s = Long.parseLong(sections[1]);
        } else {
            m = Long.parseLong(sections[0]);
        }

        long totalSecs = h * 3600 + m * 60 + s;
        if (totalSecs <= 0) throw new IllegalArgumentException("Time must be > 0");
        return totalSecs / 60.0;
    }

    private void alert(String header, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}