package com.example.scenebuilderpractice;

import java.util.Comparator;
import java.text.DecimalFormat;

public final class Run {
    String name;
    double distanceInMiles;
    double timeInMin;

    public Run(String name, double distanceMiles, double timeMinutes) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name required");
        if (distanceMiles <= 0) throw new IllegalArgumentException("Distance must be > 0");
        if (timeMinutes <= 0) throw new IllegalArgumentException("Time must be > 0");
        this.name = name.trim();
        this.distanceInMiles = distanceMiles;
        this.timeInMin = timeMinutes;
    }

    public String getName() {
        return name;
    }

    public Double getDistanceInMiles() {
        return distanceInMiles;
    }

    public Double getTimeInMin() {
        return timeInMin;
    }

    // Readable format
    public static String formatMinToHMS(double minutes) {
        long totalSeconds = Math.round(minutes * 60);
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, s);
        return String.format("%d:%02d", m, s); // mm:ss when under an hour
    }

    public static String formatMiles(double miles) {
        DecimalFormat decFormat = new DecimalFormat("0.00");
        return decFormat.format(miles);
    }

    // Getter for formatted time and pace
    public String getTimeFormatted() {
        return formatMinToHMS(timeInMin);
    }

    public String getPaceFormatted() {
        return formatMinToHMS(getPacePerMile());
    }

    public double getPacePerMile() {
        return timeInMin / distanceInMiles;
    }

    // CSV helpers
    public String toCsvLine() {
        String sName = name.contains(",") ? ("\"" + name.replace("\"", "\"\"") + "\"") : name;
        return String.join(",", sName, String.valueOf(distanceInMiles), String.valueOf(timeInMin), String.valueOf(getPacePerMile()));
    }

    // ASC & DESC formatting
    public static final Comparator<Run> BY_NAME = Comparator.comparing(Run::getName, String.CASE_INSENSITIVE_ORDER);
        public static final Comparator<Run> BY_DISTANCE_DESC = Comparator.comparingDouble(Run::getDistanceInMiles).reversed();
        public static final Comparator<Run> BY_TIME_ASC = Comparator.comparingDouble(Run::getTimeInMin);
        public static final Comparator<Run> BY_PACE_ASC = Comparator.comparingDouble(Run::getPacePerMile);
}
