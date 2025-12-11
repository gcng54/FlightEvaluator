package io.github.gcng54.flyeval.lib.geods;

import io.github.gcng54.flyeval.lib.units.*;

/**
 * A simple spherical Earth model using a mean radius.
 */
public final class EarthSph implements IEarthModel {

    public static final EarthSph INSTANCE = new EarthSph(6_371_008.8); // WGS 84 mean radius
    private final Length radius;

    public EarthSph(double meanRadiusMeters) {
        this.radius = Length.fromAltitudeMt(meanRadiusMeters);
    }

    @Override
    public Geocentric toGeocentric(Geodetic geo) {
        Length range = radius.add(geo.alt());
        return new Geocentric(geo.toCartesian(range));
    }

    @Override
    public Geodetic toGeodetic(Geocentric ecef) {
        double rangeVal = ecef.magnitude();
        if (rangeVal < 1e-9) {
            return new Geodetic(0.0, 0.0, -radius.getBase());
        }
        Angle lon = Angle.fromLongitude(
                Math.atan2(ecef.getY().getBase(), ecef.getX().getBase()), Angle.Unit.RADIAN);
        Angle lat = Angle.fromLatitude(Math.asin(ecef.getZ().getBase() / rangeVal), Angle.Unit.RADIAN);
        Length alt = Length.fromAltitudeMt(rangeVal - radius.getBase());
        return new Geodetic(lon, lat, alt);
    }

    @Override
    public Length getDistanceSurface(Geodetic p1, Geodetic p2) {
        return p1.getDistanceSurfaceHaversine(p2, radius);
    }

    @Override
    public Angle getAzimuth(Geodetic p1, Geodetic p2) {
        return p1.getAzimuthSpherical(p2);
    }

    @Override
    public Length getEarthRadius(Angle latitude) {
        return this.radius; // For a spherical model, radius is constant
    }

    @Override
    public Length getEffectiveEarthRadius(Angle latitude) {
        // Standard refraction is modeled with a k-factor of 4/3
        return getEffectiveEarthRadius(latitude, 4.0 / 3.0);
    }

    @Override
    public Length getEffectiveEarthRadius(Angle latitude, double kFactor) {
        return getEarthRadius(latitude).multiply(kFactor);
    }

    @Override
    public LatLon calculateDestination(Geodetic startPoint, Angle initialBearing, Length groundDistance) {
        double lat1 = startPoint.lat().inRadians();
        double lon1 = startPoint.lon().inRadians();
        double alpha1 = initialBearing.inRadians();
        double s = groundDistance.getBase();
        double R = this.radius.getBase(); // Use the sphere's radius

        double angDist = s / R;
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(angDist) + Math.cos(lat1) * Math.sin(angDist) * Math.cos(alpha1));
        double lon2 = lon1 + Math.atan2(Math.sin(alpha1) * Math.sin(angDist) * Math.cos(lat1), Math.cos(angDist) - Math.sin(lat1) * Math.sin(lat2));

        return new LatLon(Angle.fromLatitude(lat2, Angle.Unit.RADIAN), Angle.fromLongitude(lon2, Angle.Unit.RADIAN));
    }
}

