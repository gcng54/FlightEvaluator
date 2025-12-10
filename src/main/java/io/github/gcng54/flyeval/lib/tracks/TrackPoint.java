package io.github.gcng54.flyeval.lib.tracks;

import java.time.Instant;

import io.github.gcng54.flyeval.lib.geods.*;
import io.github.gcng54.flyeval.lib.units.*;

/**
 * Represents a single point in an aircraft's track history.
 *
 * @param icao24 The unique ICAO 24-bit address of the aircraft.
 * @param position The geodetic position of the aircraft.
 * @param timestamp The time the position was recorded.
 * @param groundSpeed The ground speed of the aircraft.
 * @param verticalRate The vertical rate (climb/descent).
 * @param onGround Whether the aircraft is on the ground.
 */
public record TrackPoint(String icao24,
                        Geodetic position,
                        Instant timestamp,
                        Speed groundSpeed,
                        Speed verticalRate,
                        boolean onGround) {}