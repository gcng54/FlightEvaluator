package io.github.gcng54.flyeval.lib.tracks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Stores and manages the sequence of track points for a single aircraft.
 */
public class AircraftTrack {
    private final String icao24;
    private final List<TrackPoint> history = new ArrayList<>();

    public AircraftTrack(String icao24) {
        this.icao24 = icao24;
    }

    public String getIcao24() {
        return icao24;
    }

    /**
     * Adds a new track point to the history.
     * Assumes points are added in rough chronological order.
     * @param point The new track point.
     */
    public void addPoint(TrackPoint point) {
        if (!point.icao24().equals(this.icao24)) {
            throw new IllegalArgumentException("Track point belongs to a different aircraft.");
        }
        // For simplicity, just add. In a real system, you might sort or handle out-of-order data.
        history.add(point);
    }

    /**
     * Gets the most recent track point.
     * @return An Optional containing the latest TrackPoint, or empty if the track is empty.
     */
    public Optional<TrackPoint> getLatestPoint() {
        if (history.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(history.get(history.size() - 1));
    }

    /**
     * Returns an unmodifiable view of the track history.
     * @return The list of track points.
     */
    public List<TrackPoint> getHistory() {
        return Collections.unmodifiableList(history);
    }
}