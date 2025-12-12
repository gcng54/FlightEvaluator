package io.github.gcng54.flyeval.lib.geods;

import org.jetbrains.annotations.NotNull;

import io.github.gcng54.flyeval.lib.units.*;

/**
 * Represents a point on Earth in Geodetic coordinates (longitude, latitude,
 * altitude).
 * This record implements {@link ISpherical} by treating longitude as
 * azimuth,
 * latitude as elevation, and altitude-plus-Earth-radius as range.
 *
 * @param lon The longitude of the point.
 * @param lat The latitude of the point.
 * @param alt The altitude (height above the spherical Earth model) of the
 *            point.
 */
public record Geodetic(Angle lon, Angle lat, Length alt) implements ISpherical<Geodetic> {

    public Geodetic {
        if (lat == null || lon == null || alt == null) {
            throw new IllegalArgumentException("Geodetic components cannot be null.");
        }
    }

    /**
     * Convenience constructor to create a Geodetic point from raw double values.
     *
     * @param lon_deg Longitude in degrees.
     * @param lat_deg Latitude in degrees.
     * @param alt_m   Altitude in meters.
     */
    public Geodetic(double lon_deg, double lat_deg, double alt_m) {
        this(Angle.ofLongitudeDeg(lon_deg),
                Angle.ofLatitudeDeg(lat_deg),
                Length.ofAltitudeMt(alt_m));
    }

    @Override
    /** {@inheritDoc} (Longitude is treated as Azimuth) */
    public Angle getAzimuth() {
        return this.lon;
    }

    @Override
    /** {@inheritDoc} (Latitude is treated as Elevation) */
    public Angle getElevation() {
        return this.lat;
    }

    @Override
    /** {@inheritDoc} (Range is the distance from the Earth's center) */
    public Length getRange() { // Note: This is less meaningful for an ellipsoid model.
        return Length.ofMeter(Earth84.A).add(this.alt);
    }

    @Override
    /**
     * {@inheritDoc}
     * Creates a new Geodetic point from spherical components.
     */
    public Geodetic create(Angle azimuth, Angle elevation, Length range) {
        return new Geodetic(azimuth, elevation, range.subtract(Length.ofMeter(Earth84.A)));
    }

    /**
     * Converts Geodetic coordinates to Geocentric (ECEF) coordinates.
     * This assumes a spherical Earth model.
     *
     * @return A new Geocentric object.
     */
    public Geocentric toGeocentric() {
        return IEarthModel.EARTH_MODEL.toGeocentric(this); // NOSONAR
    }

    /**
     * Transforms this Geodetic point by a Cartesian displacement vector.
     *
     * @param displacement The Cartesian vector to add.
     * @return A new Geodetic point representing the result of the transformation.
     */
    public Geodetic transform(Cartesian displacement) {
        Geocentric startEcef = this.toGeocentric();
        Geocentric endEcef = startEcef.add(displacement);
        return endEcef.toGeodetic();
    }

    /**
     * Calculates the straight-line (chord) distance to another Geodetic point.
     * This is done by converting both points to Geocentric (ECEF) coordinates
     * and calculating the 3D Euclidean distance.
     *
     * @param other The other Geodetic point.
     * @return The distance as a Types.Length quantity.
     */
    public Length getDistance(Geodetic other) { // NOSONAR
        Geocentric thisEcef = this.toGeocentric();
        Geocentric otherEcef = other.toGeocentric();
        double distanceInMeters = thisEcef.hypotXYZ(otherEcef);
        return Length.ofDistance(distanceInMeters, Length.Unit.METER);
    }

    /**
     * Calculates the Cartesian displacement vector from this point to another
     * Geodetic point.
     * The resulting vector is in the Earth-Centered, Earth-Fixed (ECEF) frame.
     *
     * @param other The target Geodetic point.
     * @return The displacement as a Cartesian object.
     */
    public Cartesian getCartesian(Geodetic other) {
        Geocentric thisEcef = this.toGeocentric();
        Geocentric otherEcef = other.toGeocentric();
        Geocentric displacement = otherEcef.subtract(thisEcef);
        return displacement.toCartesian();
    }

    /**
     * Calculates the Spherical coordinates (azimuth, elevation, range) of the
     * vector
     * from this point to another Geodetic point.
     * While intermediate calculations use the Earth-Centered, Earth-Fixed (ECEF)
     * frame,
     * the final spherical coordinates are relative to the local East-North-Up (ENU)
     * frame at this point.
     *
     * @param other The target Geodetic point.
     * @return The relative position as a Spherical object.
     */
    public Spherical getSpherical(Geodetic other) {
        // Get the cartesian displacement vector first
        Cartesian displacement = this.toENU(this.getCartesian(other));
        // Convert the cartesian vector to spherical coordinates
        return displacement.toSpherical();
    }

    /**
     * Converts an ECEF displacement vector to a local East-North-Up (ENU) frame
     * relative to this geodetic point.
     * The ENU vector represents the displacement in terms of East, North, and Up
     * components from the origin.
     *
     * @param ecefVector The Cartesian displacement vector in the ECEF frame.
     * @return The displacement vector in the local ENU frame as a new Cartesian
     *         object.
     */ // NOSONAR
    public Cartesian toENU(Cartesian ecefVector) {

        double sinLon = this.lon().sin();
        double cosLon = this.lon().cos();
        double sinLat = this.lat().sin();
        double cosLat = this.lat().cos();

        double dx = ecefVector.getX().getBase();
        double dy = ecefVector.getY().getBase();
        double dz = ecefVector.getZ().getBase();

        // The rotation matrix from ECEF to ENU is the transpose of the ENU-to-ECEF matrix.

        double east = -sinLon * dx + cosLon * dy;
        double north = -sinLat * cosLon * dx - sinLat * sinLon * dy + cosLat * dz;
        double up = cosLat * cosLon * dx + cosLat * sinLon * dy + sinLat * dz;

        return new Cartesian(Length.ofMeter(east), Length.ofMeter(north), Length.ofMeter(up));
    }

    public Cartesian toECEF(Cartesian enu) {
        double east = enu.getX().inMeter();
        double north = enu.getY().inMeter();
        double up = enu.getZ().inMeter();

        double sinLon = this.lon().sin();
        double cosLon = this.lon().cos();
        double sinLat = this.lat().sin();
        double cosLat = this.lat().cos();

        double x = -sinLon * east - sinLat * cosLon * north + cosLat * cosLon * up;
        double y = cosLon * east - sinLat * sinLon * north + cosLat * sinLon * up;
        double z = cosLat * north + sinLat * up;

        return new Cartesian(Length.ofMeter(x), Length.ofMeter(y), Length.ofMeter(z));
    }

    /**
     * Calculates the great-circle distance (surface distance) to another Geodetic
     * point at sea level using the active EARTH_MODEL.
     * This method uses the Haversine formula for accuracy and ignores the altitude
     * of both points.
     *
     * @param other The other Geodetic point.
     * @return The surface distance as a Types.Length quantity.
     */
    public Length getDistanceSurface(Geodetic other) {
        return IEarthModel.EARTH_MODEL.getDistanceSurface(this, other);
    }

    /**
     * Calculates great-circle distance using the Haversine formula on a sphere.
     * 
     * @param other  The other point.
     * @param radius The radius of the sphere.
     * @return The surface distance.
     */
    public Length getDistanceSurfaceHaversine(Geodetic other, Length radius) {
        double lat1 = this.lat.inRadians();
        double lat2 = other.lat.inRadians();
        double dLat = lat2 - lat1;
        double dLon = other.lon.inRadians() - this.lon.inRadians();

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distanceInMeters = radius.getBase() * c;
        return Length.ofDistance(distanceInMeters, Length.Unit.METER);
    }

    public Length getDistanceSurfaceHaversine(Geodetic other) {
        return getDistanceSurfaceHaversine(other, Length.ofMeter(Earth84.A)); // Use semi-major axis as
                                                                                // approx.
    }

    /**
     * Calculates the initial bearing (azimuth) from this point to another Geodetic
     * point.
     * The azimuth is the angle measured clockwise from North.
     *
     * @param other The target Geodetic point.
     * @return The initial bearing as an Types.Angle with the AZIMUTH semantic
     *         (0-360 degrees).
     */
    public Angle getAzimuth(Geodetic other) {
        return IEarthModel.EARTH_MODEL.getAzimuth(this, other);
    }

    /** Calculates bearing on a spherical model. */
    public Angle getAzimuthSpherical(Geodetic other) {
        double lat1 = this.lat.inRadians();
        double lat2 = other.lat.inRadians();
        double dLon = other.lon.inRadians() - this.lon.inRadians();

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        double bearingRadians = Math.atan2(y, x);
        return Angle.ofAzimuth(bearingRadians, Angle.Unit.RADIAN);
    }

    /**
     * Calculates the elevation angle from this Geodetic point to another.
     * The elevation angle is the angle above the local horizontal (tangent to the
     * Earth's surface at this point)
     * to the line connecting this point to the other point, taking into account
     * both surface distance and altitude difference.
     * <p>
     * Note: This is not the same as the altitude of this point; it is the angle to
     * the other point.
     *
     * @param other The target Geodetic point.
     * @return The elevation angle to the other point as a Types.Angle with the
     *         ELEVATION semantic (positive above the horizon, negative below).
     */
    public Angle getElevation(Geodetic other) {
        Length horizontalDistance = this.getDistanceSurface(other);
        Length altDiff = other.alt.subtract(this.alt);
        if (horizontalDistance.getBase() < 1e-9) {
            return altDiff.getBase() >= 0 ? Angle.ofElevationDeg(90.0)
                    : Angle.ofElevationDeg(-90.0);
        }
        double elevationRad = Math.atan2(altDiff.getBase(), horizontalDistance.getBase());
        return Angle.ofElevation(elevationRad, Angle.Unit.RADIAN);
    }

    public Length getAltitudeDifference(Geodetic other) {
        return other.alt.subtract(this.alt);
    }

    public @NotNull String toString() {
        return String.format("%s %s %s",
                lat.toDMSString(),
                lon.toDMSString(),
                alt.create(Length.Unit.METER));
    }
}
