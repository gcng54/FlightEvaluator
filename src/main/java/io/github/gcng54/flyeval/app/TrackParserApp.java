package io.github.gcng54.flyeval.app;


import java.io.File;
import java.io.IOException;
import java.util.Map;

import io.github.gcng54.flyeval.lib.tracks.*;

/**
 * A simple application to demonstrate parsing of ADS-B track data from a CSV file.
 */
public class TrackParserApp {
    public static void main(String[] args) {
        // Use a relative path to the data file within the project structure.
        File dataFile = new File("data/sample_adsb_data.csv");

        if (!dataFile.exists()) {
            System.err.println("Error: The data file was not found at " + dataFile.getAbsolutePath());
            System.err.println("Please create the file with sample CSV data.");
            return;
        }

        try {
            Map<String, AircraftTrack> allTracks = TrackCsvParser.parse(dataFile);

            // Now you have all the tracks loaded from the file
            System.out.println("Successfully parsed " + allTracks.size() + " unique aircraft tracks.");

            // Iterate through the parsed tracks and print a summary
            allTracks.forEach((icao, track) -> {
                System.out.println("\n--- Track for ICAO: " + icao + " ---");
                System.out.println("Total points: " + track.getHistory().size());
                track.getLatestPoint().ifPresent(latestPoint ->
                    System.out.println("Latest position: " + latestPoint.position())
                );
            });

        } catch (IOException e) {
            System.err.println("Failed to read the track file: " + e.getMessage());
        }
    }
}

/**
 * Successfully parsed 2 unique aircraft tracks.

--- Track for ICAO: a4f2c1 ---
Total points: 3
Latest position: 51.6°0'0.00"N 0°7'48.00"W 9400.000 m

--- Track for ICAO: a83568 ---
Total points: 4
Latest position: 51.4703°0'0.00"N 0.342°0'0.00"W 10000.000 m {

time: The moment the data was recorded, as a Unix timestamp in seconds.
icao24: The unique 6-character hexadecimal ID of the aircraft.
lat: Latitude in decimal degrees.
lon: Longitude in decimal degrees.
baro_altitude: The barometric altitude in meters.
on_ground: A boolean (true or false) indicating if the aircraft is on the ground.
velocity: The ground speed in meters per second.
vertical_rate: The rate of climb or descent in meters per second (positive for climb, negative for descent).

*/