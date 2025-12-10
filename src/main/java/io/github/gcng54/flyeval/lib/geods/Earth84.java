package io.github.gcng54.flyeval.lib.geods;

import io.github.gcng54.flyeval.lib.units.*;

/**
 * The World Geodetic System 1984 (WGS84) ellipsoidal model.
 * This is the standard for GPS and most modern GIS applications.
 */
public final class Earth84 implements IEarthModel {

    public static final Earth84 INSTANCE = new Earth84();

    public static final double A = 6378137.0; // Semi-major axis in meters
    public static final double F = 1.0 / 298.257223563; // Flattening
    public static final double B = A * (1.0 - F); // Semi-minor axis
    private static final double E_SQ = (A * A - B * B) / (A * A); // Eccentricity squared

    private Earth84() {
    }

    @Override
    public Geocentric toGeocentric(Geodetic geo) {
        double latRad = geo.lat().inRadians();
        double lonRad = geo.lon().inRadians();
        double altM = geo.alt().inMeter();

        double n = A / Math.sqrt(1.0 - E_SQ * Math.sin(latRad) * Math.sin(latRad));
        double x = (n + altM) * Math.cos(latRad) * Math.cos(lonRad);
        double y = (n + altM) * Math.cos(latRad) * Math.sin(lonRad);
        double z = ((1.0 - E_SQ) * n + altM) * Math.sin(latRad);

        return new Geocentric(Length.fromMeter(x), Length.fromMeter(y), Length.fromMeter(z));
    }

    @Override
    public Geodetic toGeodetic(Geocentric ecef) {
        double x = ecef.getX().getBase();
        double y = ecef.getY().getBase();
        double z = ecef.getZ().getBase();
        double p = Math.sqrt(x * x + y * y);

        Angle lon = Angle.fromLongitude(Math.atan2(y, x), Angle.Unit.RADIAN);

        // Iterative method for latitude and altitude (Bowring's formula or similar)
        double latRad = Math.atan2(z, p * (1.0 - E_SQ));
        double altM;
        for (int i = 0; i < 5; i++) { // 5 iterations is more than enough for millimeter accuracy
            double sinLat = Math.sin(latRad);
            double n = A / Math.sqrt(1.0 - E_SQ * sinLat * sinLat);
            altM = p / Math.cos(latRad) - n;
            latRad = Math.atan2(z, p * (1.0 - E_SQ * n / (n + altM)));
        }
        double sinLat = Math.sin(latRad);
        double n = A / Math.sqrt(1.0 - E_SQ * sinLat * sinLat);
        altM = p / Math.cos(latRad) - n;

        return new Geodetic(lon, Angle.fromLatitude(latRad, Angle.Unit.RADIAN), Length.fromAltitudeMeter(altM));
    }

    @Override
    public Length getDistanceSurface(Geodetic p1, Geodetic p2) {
        // Implementation of Vincenty's inverse formula for distance
        // (A more robust implementation might use Karney's method, but Vincenty is a
        // classic)
        double lat1 = p1.lat().inRadians();
        double lon1 = p1.lon().inRadians();
        double lat2 = p2.lat().inRadians();
        double lon2 = p2.lon().inRadians();

        double U1 = Math.atan((1 - F) * Math.tan(lat1));
        double U2 = Math.atan((1 - F) * Math.tan(lat2));
        double L = lon2 - lon1;
        double lambda = L;
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

        double sinSigma, cosSigma, sigma, sinAlpha, cosSqAlpha, C;
        double lambdaP;
        int iterLimit = 100;
        do {
            double sinLambda = Math.sin(lambda);
            double cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
                    + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
            if (sinSigma == 0)
                return Length.fromAltitudeMeter(0.0); // Co-incident points
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha * sinAlpha;
            cosSigma = (cosSqAlpha != 0) ? (cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha) : 0; // Handle equatorial
                                                                                            // lines
            C = F / 16 * cosSqAlpha * (4 + F * (4 - 3 * cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1 - C) * F * sinAlpha
                    * (sigma + C * sinSigma * (cosSigma + C * cosSigma * (-1 + 2 * cosSigma * cosSigma)));
        } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

        if (iterLimit == 0)
            return p1.getDistanceSurfaceHaversine(p2); // Fallback to Haversine if no convergence

        double uSq = cosSqAlpha * (A * A - B * B) / (B * B);
        double AA = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double BB = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma = BB * sinSigma * (cosSigma + BB / 4 * (cosSigma * (-1 + 2 * cosSigma * cosSigma)
                - BB / 6 * cosSigma * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cosSigma * cosSigma)));

        double s = B * AA * (sigma - deltaSigma);
        return Length.fromAltitudeMeter(s);
    }

    @Override
    public Angle getAzimuth(Geodetic p1, Geodetic p2) {
        // Part of Vincenty's inverse formula for bearing
        double lat1 = p1.lat().inRadians();
        double lon1 = p1.lon().inRadians();
        double lat2 = p2.lat().inRadians();
        double lon2 = p2.lon().inRadians();

        double U1 = Math.atan((1 - F) * Math.tan(lat1));
        double U2 = Math.atan((1 - F) * Math.tan(lat2));
        double L = lon2 - lon1;
        double lambda = L;

        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

        double lambdaP;
        int iterLimit = 100;
        do {
            double sinLambda = Math.sin(lambda);
            double cosLambda = Math.cos(lambda);
            double sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
                    + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
            if (sinSigma == 0)
                return Angle.fromAzimuthDeg(0.0);
            double cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            double sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            double cosSqAlpha = 1 - sinAlpha * sinAlpha;
            cosSigma = (cosSqAlpha != 0) ? (cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha) : 0;
            double C = F / 16 * cosSqAlpha * (4 + F * (4 - 3 * cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1 - C) * F * sinAlpha
                    * (sigma + C * sinSigma * (cosSigma + C * cosSigma * (-1 + 2 * cosSigma * cosSigma)));
        } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

        double y = Math.sin(lambda) * cosU2;
        double x = cosU1 * sinU2 - sinU1 * cosU2 * Math.cos(lambda);

        return Angle.fromAzimuth(Math.atan2(y, x), Angle.Unit.RADIAN);
    }

    @Override
    public Length getEarthRadius(Angle latitude) {
        double latRad = latitude.inRadians();
        double cosLat = Math.cos(latRad);
        double sinLat = Math.sin(latRad);

        // Formula for the radius of an ellipsoid at a given geodetic latitude
        double num = (A * A * cosLat) * (A * A * cosLat) + (B * B * sinLat) * (B * B * sinLat);
        double den = (A * cosLat) * (A * cosLat) + (B * sinLat) * (B * sinLat);

        if (den < 1e-10)
            return Length.fromAltitudeMeter(B); // At the pole

        return Length.fromAltitudeMeter(Math.sqrt(num / den));
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

        double sinAlpha1 = Math.sin(alpha1);
        double cosAlpha1 = Math.cos(alpha1);

        double f = Earth84.F; // WGS84 flattening
        double b = Earth84.B; // Semi-minor axis

        double tanU1 = (1 - f) * Math.tan(lat1);
        double cosU1 = 1 / Math.sqrt((1 + tanU1 * tanU1));
        double sinU1 = tanU1 * cosU1;
        double sigma1 = Math.atan2(tanU1, cosAlpha1);
        double sinAlpha = cosU1 * sinAlpha1;
        double cosSqAlpha = 1 - sinAlpha * sinAlpha;
        double uSq = cosSqAlpha * (A * A - b * b) / (b * b);
        double AA = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double BB = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));

        double sigma = s / (b * AA);
        double sigmaP;
        double cos2SigmaM;
        double sinSigma;
        int iterLimit = 100;
        do {
            cos2SigmaM = Math.cos(2 * sigma1 + sigma);
            sinSigma = Math.sin(sigma);
            double deltaSigma = BB * sinSigma * (cos2SigmaM + BB / 4 * (Math.cos(sigma) *
                    (-1 + 2 * cos2SigmaM * cos2SigmaM)
                    - BB / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) *
                            (-3 + 4 * cos2SigmaM * cos2SigmaM)));
            sigmaP = sigma;
            sigma = s / (b * AA) + deltaSigma;
            if (--iterLimit == 0) {
                break; // Failed to converge
            }
        } while (Math.abs(sigma - sigmaP) > 1e-12);

        double lat2, lon2;

        if (iterLimit > 0) {
            // Vincenty formula converged, calculate final lat/lon
            sinSigma = Math.sin(sigma);
            cos2SigmaM = Math.cos(2 * sigma1 + sigma);
            double cosSigma = Math.cos(sigma);

            double tmp = sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1;
            lat2 = Math.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1,
                    (1 - f) * Math.sqrt(sinAlpha * sinAlpha + tmp * tmp));
            double lambda = Math.atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1);
            double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
            double L = lambda - (1 - C) * f * sinAlpha
                    * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
            lon2 = lon1 + L;
        } else {
            // Fallback to Spherical model if Vincenty's formula fails to converge
            // Use the spherical model's calculateDestination
            return EarthSph.INSTANCE.calculateDestination(startPoint, initialBearing, groundDistance);
        }

        return new LatLon(
                Angle.fromLatitude(lat2, Angle.Unit.RADIAN),
                Angle.fromLongitude(lon2, Angle.Unit.RADIAN));
    }
}
