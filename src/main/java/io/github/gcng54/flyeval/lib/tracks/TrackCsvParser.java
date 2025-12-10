package io.github.gcng54.flyeval.lib.tracks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import io.github.gcng54.flyeval.lib.geods.*;
import io.github.gcng54.flyeval.lib.units.*;


/**
 * A utility class to parse ADS-B track data from a CSV file.
 */
public final class TrackCsvParser {

    private TrackCsvParser() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Parses a CSV file containing track data and organizes it into AircraftTrack objects.
     *
     * The expected CSV format is:
     * {@code time,icao24,lat,lon,baro_altitude,on_ground,velocity,vertical_rate}
     * Where:
     * - time: Unix timestamp in seconds.
     * - icao24: ICAO 24-bit address (hex string).
     * - lat: Latitude in decimal degrees.
     * - lon: Longitude in decimal degrees.
     * - baro_altitude: Barometric altitude in meters.
     * - on_ground: Boolean (true/false).
     * - velocity: Ground speed in meters/second.
     * - vertical_rate: Vertical rate in meters/second (positive for climb).
     *
     * @param csvFile The CSV file to parse.
     * @return A map where the key is the ICAO24 address and the value is the corresponding AircraftTrack.
     * @throws IOException if an I/O error occurs reading from the file.
     */
    public static Map<String, AircraftTrack> parse(File csvFile) throws IOException {
        Map<String, AircraftTrack> tracks = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            // Skip header line
            br.readLine();

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    TrackPoint point = parseLine(line);
                    // Get the existing track for this ICAO or create a new one
                    AircraftTrack track = tracks.computeIfAbsent(point.icao24(), AircraftTrack::new);
                    track.addPoint(point);
                } catch (Exception e) {
                    // Log or handle malformed lines. For now, we'll print an error and continue.
                    System.err.println("Skipping malformed line: " + line + " | Error: " + e.getMessage());
                }
            }
        }

        return tracks;
    }

    /**
     * Parses a single line of CSV data into a TrackPoint.
     *
     * @param line The string line from the CSV file.
     * @return A new TrackPoint object.
     * @throws NumberFormatException if numeric fields are invalid.
     * @throws ArrayIndexOutOfBoundsException if the line doesn't have enough columns.
     */
    private static TrackPoint parseLine(String line) {
        String[] parts = line.split(",");

        // Column 0: time (Unix timestamp in seconds)
        long timestampSeconds = Long.parseLong(parts[0].trim());
        Instant timestamp = Instant.ofEpochSecond(timestampSeconds);

        // Column 1: icao24
        String icao24 = parts[1].trim();

        // Column 2 & 3: lat, lon
        double lat = Double.parseDouble(parts[2].trim());
        double lon = Double.parseDouble(parts[3].trim());

        // Column 4: baro_altitude (meters)
        double altMeters = Double.parseDouble(parts[4].trim());
        Geodetic position = new Geodetic(lon, lat, altMeters);

        // Column 5: on_ground
        boolean onGround = Boolean.parseBoolean(parts[5].trim());

        // Column 6: velocity (m/s)
        double groundSpeedMps = Double.parseDouble(parts[6].trim());
        // Convert m/s to knots for the Speed object (a common aviation unit)
        Speed groundSpeed = new Speed(groundSpeedMps * 1.94384, Speed.Unit.NAUTICAL_HR);

        // Column 7: vertical_rate (m/s)
        double verticalRateMps = Double.parseDouble(parts[7].trim());
        // Convert m/s to ft/min for the Speed object
        Speed verticalRate = new Speed(verticalRateMps * 196.85, Speed.Unit.FOOT_HR).create(Speed.Unit.FOOT_HR).multiply(60);

        return new TrackPoint(icao24, position, timestamp, groundSpeed, verticalRate, onGround);
    }
}