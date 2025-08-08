package com.example.scenebuilderpractice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class RunStore {
    private final ObservableList<Run> all = FXCollections.observableArrayList();
    private final SortedList<Run> sortedList = new SortedList<>(all);

    public SortedList<Run> getView() {
        return sortedList;
    }

    public void add(Run r) {
        all.add(r);
    }

    public void setComparator(Comparator<Run> cmp, boolean ascending) {
        if (!ascending) cmp = cmp.reversed();
        sortedList.setComparator(cmp);
    }

    public void exportCsv(Path file) throws IOException {
        try (BufferedWriter buffW = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            buffW.write("name,distance_miles,time_minutes,pace_min_per_mile\n");
            for (Run r : all) {
                buffW.write(r.toCsvLine());
                buffW.write('\n');
            }
        }
    }
}