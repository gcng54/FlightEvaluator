package io.github.gcng54.flyeval.lib.geods;

import io.github.gcng54.flyeval.lib.units.*;

/**
 * Record representing a pair of latitude and longitude angles.
 * Used for returning geodetic coordinates without altitude.
 */
public record LatLon(Angle lat, Angle lon) {}
