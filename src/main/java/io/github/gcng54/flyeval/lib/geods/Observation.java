package io.github.gcng54.flyeval.lib.geods;

import io.github.gcng54.flyeval.lib.units.*;

/**
 * Record representing a radar observation with Azimuth, Range, and Altitude.
 * This is distinct from Spherical coordinates, which use Elevation instead of Altitude.
 *
 * @param azimuth   The azimuth angle from the radar.
 * @param range     The slant range from the radar.
 * @param altitude  The altitude of the observation.
 */
public record Observation(Angle azimuth, Length range, Length altitude) {

    public static final Observation ZERO = new Observation(
            Angle.fromAzimuthDeg(0.0),
            Length.fromRangeKm(0.0),
            Length.fromAltitudeMt(0.0));

    public Observation {
        if (azimuth == null || range == null || altitude == null) {
            throw new IllegalArgumentException("Observation components cannot be null.");
        }
    }
}
