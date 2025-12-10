package io.github.gcng54.flyeval.lib.tracks;

import java.util.List;

import io.github.gcng54.flyeval.lib.geods.*;

/**
 * Represents a planned flight route, defined by a series of waypoints.
 *
 * @param routeId An identifier for the route.
 * @param waypoints An ordered list of Geodetic points defining the route legs.
 */
public record FlightRoute(String routeId, List<Geodetic> waypoints) {
    public FlightRoute {
        if (waypoints == null || waypoints.size() < 2) {
            throw new IllegalArgumentException("A flight route must have at least two waypoints.");
        }
    }

    /**
     * Gets the number of waypoints in the route.
     * @return The count of waypoints.
     */
    public int getWaypointCount() {
        return waypoints.size();
    }

    /**
     * Gets a specific leg of the route. A leg is a segment between two consecutive waypoints.
     * @param legIndex The index of the leg (0 to getWaypointCount() - 2).
     * @return A record containing the start and end waypoints of the leg.
     * @throws IndexOutOfBoundsException if the leg index is invalid.
     */
    public RouteLeg getLeg(int legIndex) {
        if (legIndex < 0 || legIndex >= getWaypointCount() - 1) {
            throw new IndexOutOfBoundsException("Invalid leg index: " + legIndex);
        }
        return new RouteLeg(waypoints.get(legIndex), waypoints.get(legIndex + 1));
    }

    /**
     * Represents a single leg of a flight route.
     */
    public record RouteLeg(Geodetic start, Geodetic end) {}
}

