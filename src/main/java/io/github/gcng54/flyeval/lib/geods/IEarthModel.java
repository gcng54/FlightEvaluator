package io.github.gcng54.flyeval.lib.geods;

import io.github.gcng54.flyeval.lib.units.*;

/**
 * Interface for defining an Earth model, allowing for different levels of
 * fidelity (e.g., Spherical vs. Ellipsoidal).
 */
public interface IEarthModel {

    /** The currently active Earth model. Defaults to WGS84. */
    public static IEarthModel EARTH_MODEL = Earth84.INSTANCE;

    /**
     * Converts geodetic coordinates to geocentric (ECEF) coordinates.
     * 
     * @param geo The geodetic point.
     * @return The corresponding geocentric point.
     */
    Geocentric toGeocentric(Geodetic geo);

    /**
     * Converts geocentric (ECEF) coordinates to geodetic coordinates.
     * 
     * @param ecef The geocentric point.
     * @return The corresponding geodetic point.
     */
    Geodetic toGeodetic(Geocentric ecef);

    /**
     * Calculates the surface distance between two geodetic points.
     * 
     * @param p1 The starting point.
     * @param p2 The ending point.
     * @return The surface distance as a Length quantity.
     */
    Length getDistanceSurface(Geodetic p1, Geodetic p2);

    /**
     * Calculates the initial bearing (forward azimuth) from one point to another.
     * 
     * @param p1 The starting point.
     * @param p2 The ending point.
     * @return The initial bearing as an Angle quantity.
     */
    Angle getAzimuth(Geodetic p1, Geodetic p2);

    /**
     * Calculates the Earth's radius for a given latitude.
     * 
     * @param latitude The geodetic latitude.
     * @return The Earth's radius at that latitude as a Length quantity.
     */
    Length getEarthRadius(Angle latitude);

    /**
     * Calculates the effective Earth radius for a given latitude, accounting for
     * standard atmospheric refraction (k=4/3).
     * This is crucial for radar line-of-sight and coverage calculations.
     * 
     * @param latitude The geodetic latitude.
     * @return The effective Earth's radius at that latitude.
     */
    Length getEffectiveEarthRadius(Angle latitude);

    /**
     * Calculates the effective Earth radius for a given latitude with a custom
     * k-factor.
     * 
     * @param latitude The geodetic latitude.
     * @return The effective Earth's radius for the given k-factor.
     */
    Length getEffectiveEarthRadius(Angle latitude, double kFactor);

    /**
     * Calculates the destination geodetic coordinates (latitude and longitude)
     * given a starting point, an initial bearing, and a ground distance.
     * The altitude of the returned point is not determined by this method.
     *
     * @param startPoint The starting geodetic point (altitude is ignored for surface calculation).
     * @param initialBearing The initial bearing (azimuth) from the start point.
     * @param groundDistance The ground distance to travel.
     * @return A {@link LatLon} record containing the calculated latitude and longitude.
     */
    LatLon calculateDestination(Geodetic startPoint, Angle initialBearing, Length groundDistance);
}