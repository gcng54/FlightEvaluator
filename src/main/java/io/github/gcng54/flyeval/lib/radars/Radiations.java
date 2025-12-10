package io.github.gcng54.flyeval.lib.radars;

import io.github.gcng54.flyeval.lib.units.*;

import java.util.List;
import java.util.stream.Collectors;

import io.github.gcng54.flyeval.lib.geods.*;

/**
 * Provides utility methods for radar-specific calculations, such as converting
 * detections to geodetic coordinates.
 */
public final class Radiations {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Radiations() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * The standard mean radius of the Earth (WGS 84), used for many radar calculations.
     */
    public static final Length EARTH_RADIUS = Length.fromDistanceKm(6371.0088);

    /**
     * The standard atmospheric refraction k-factor, commonly approximated as 4/3.
     * This value is used to model the bending of radar waves in the atmosphere,
     * allowing for calculations over a larger, "effective" Earth radius.
     */
    public static final double STANDARD_REFRACTION_K = 4.0 / 3.0;

    /**
     * Converts a target's geodetic position to spherical coordinates (az, el, range)
     * relative to a radar's geodetic position, accounting for standard atmospheric
     * refraction (k=4/3).
     *
     * @param radarPos  The geodetic position of the radar.
     * @param observation The radar observation (azimuth, range, altitude).
     * @return The target's apparent spherical coordinates (with elevation) relative to the radar.
     */
    public static Spherical toSpherical(Geodetic radarPos, Observation observation) {
        return toSpherical(radarPos, observation, STANDARD_REFRACTION_K);
    }

    /**
     * Converts a radar observation (azimuth, range, altitude) to spherical coordinates (az, el, range)
     * relative to a radar's geodetic position, accounting for a specified atmospheric refraction factor.
     *
     * @param radarPos    The geodetic position of the radar.
     * @param observation The radar observation (azimuth, range, altitude).
     * @param kFactor     The atmospheric refraction k-factor.
     * @return The target's apparent spherical coordinates (with elevation) relative to the radar.
     */
    public static Spherical toSpherical(Geodetic radarPos, Observation observation, double kFactor) {
        // 1. Azimuth and Range are taken directly from the observation
        Angle azimuth = observation.azimuth();
        Length slantRange = observation.range();

        // 2. Calculate Apparent Elevation Angle from altitudes and range
        Length radarAlt = radarPos.alt();
        Length targetAlt = observation.altitude();
        Angle radarLat = radarPos.lat();

        // Get effective radius at the radar's location
        Length effectiveRadius = IEarthModel.EARTH_MODEL.getEffectiveEarthRadius(radarLat, kFactor);

        // Solve the geometry for elevation
        RefractionTriangleResult result = solveRefractionTriangle(effectiveRadius, radarAlt, targetAlt, slantRange, null);
        Angle elevation = result.elevation();

        return new Spherical(azimuth, elevation, slantRange);
    }


    /**
     * Converts a target's geodetic position to spherical coordinates (az, el, range)
     * relative to a radar's geodetic position, accounting for standard atmospheric
     * refraction (k=4/3).
     *
     * @param radarPos  The geodetic position of the radar.
     * @param targetPos The geodetic position of the target.
     * @return The target's apparent spherical coordinates relative to the radar.
     */ // NOSONAR
    public static Spherical toSpherical(Geodetic radarPos, Geodetic targetPos) {
        return toSpherical(radarPos, targetPos, STANDARD_REFRACTION_K);
    }

    /**
     * Converts a target's geodetic position to spherical coordinates (az, el, range)
     * relative to a radar's geodetic position, accounting for a specified
     * atmospheric refraction factor.
     *
     * @param radarPos  The geodetic position of the radar.
     * @param targetPos The geodetic position of the target.
     * @param kFactor   The atmospheric refraction k-factor (e.g., 4/3 for standard refraction).
     * @return The target's apparent spherical coordinates relative to the radar.
     */ // NOSONAR
    public static Spherical toSpherical(Geodetic radarPos, Geodetic targetPos, double kFactor) {
        // 1. Calculate Slant Range (chord distance)
        // This is the true geometric distance between the two 3D points.
        Length slantRange = radarPos.getDistance(targetPos);

        // 2. Calculate Azimuth
        // This is the initial bearing from the radar to the target.
        Angle azimuth = radarPos.getAzimuth(targetPos);

        // 3. Calculate Apparent Elevation Angle
        // This requires using the effective Earth radius to model refraction.
        Length radarAlt = radarPos.alt();
        Length targetAlt = targetPos.alt();
        Angle radarLat = radarPos.lat();

        // Get effective radius at the radar's location
        Length effectiveRadius = IEarthModel.EARTH_MODEL.getEffectiveEarthRadius(radarLat, kFactor); // NOSONAR

        // Solve the geometry
        RefractionTriangleResult result = solveRefractionTriangle(effectiveRadius, radarAlt, targetAlt, slantRange, null);
        Angle elevation = result.elevation();

        return new Spherical(azimuth, elevation, slantRange);
    }

    /**
     * Converts a list of target geodetic positions to spherical coordinates (az, el, range)
     * relative to a radar's geodetic position, accounting for standard atmospheric
     * refraction (k=4/3).
     *
     * @param radarPos The geodetic position of the radar.
     * @param targetPositions A list of geodetic positions of the targets.
     * @return A list of the targets' apparent spherical coordinates relative to the radar.
     */
    public static List<Spherical> toSphericals(Geodetic radarPos, List<Geodetic> targetPositions) {
        return toSphericals(radarPos, targetPositions, STANDARD_REFRACTION_K);
    }

    /**
     * Converts a list of target geodetic positions to spherical coordinates (az, el, range)
     * relative to a radar's geodetic position, accounting for a specified
     * atmospheric refraction factor.
     *
     * @param radarPos The geodetic position of the radar.
     * @param targetPositions A list of geodetic positions of the targets.
     * @param kFactor The atmospheric refraction k-factor (e.g., 4/3 for standard refraction).
     * @return A list of the targets' apparent spherical coordinates relative to the radar.
     */
    public static List<Spherical> toSphericals(Geodetic radarPos, List<Geodetic> targetPositions, double kFactor) {
        if (targetPositions == null) {
            throw new IllegalArgumentException("Target positions list cannot be null.");
        }
        return targetPositions.stream()
                .map(targetPos -> toSpherical(radarPos, targetPos, kFactor))
                .collect(Collectors.toList());
    }


    /**
     * Converts a radar detection in spherical coordinates (relative to the radar)
     * to a geodetic position on Earth, accounting for Earth curvature and
     * standard atmospheric refraction (k=4/3).
     *
     * @param radarPos    The geodetic position of the radar.
     * @param targetSph   The target's spherical coordinates (az, el, range) relative to the radar.
     * @return The calculated geodetic position of the target.
     */ // NOSONAR
    public static Geodetic toGeodetic(Geodetic radarPos, Spherical targetSph) {
        // Default to standard atmospheric refraction
        return toGeodetic(radarPos, targetSph, STANDARD_REFRACTION_K);
    }

    /**
     * Converts a radar observation (azimuth, range, altitude) to a geodetic position on Earth,
     * accounting for Earth curvature and standard atmospheric refraction (k=4/3).
     * This is a two-step conversion: Observation -> Spherical -> Geodetic.
     *
     * @param radarPos    The geodetic position of the radar.
     * @param observation The radar observation.
     * @return The calculated geodetic position of the target.
     */
    public static Geodetic toGeodetic(Geodetic radarPos, Observation observation) {
        return toGeodetic(radarPos, observation, STANDARD_REFRACTION_K);
    }

    /**
     * Converts a radar observation (azimuth, range, altitude) to a geodetic position on Earth,
     * accounting for Earth curvature and a specified atmospheric refraction factor.
     * This is a two-step conversion: Observation -> Spherical -> Geodetic.
     *
     * @param radarPos    The geodetic position of the radar.
     * @param observation The radar observation.
     * @param kFactor     The atmospheric refraction k-factor.
     * @return The calculated geodetic position of the target.
     */
    public static Geodetic toGeodetic(Geodetic radarPos, Observation observation, double kFactor) {
        // First, convert the observation to spherical coordinates to get the elevation angle.
        Spherical targetSph = toSpherical(radarPos, observation, kFactor);

        // Then, convert the resulting spherical coordinates to a geodetic position.
        return toGeodetic(radarPos, targetSph, kFactor);
    }

    /**
     * Converts a target's geodetic position to a radar observation (azimuth, range, altitude).
     *
     * @param radarPos  The geodetic position of the radar.
     * @param targetPos The geodetic position of the target.
     * @return The radar observation.
     */
    public static Observation toObservation(Geodetic radarPos, Geodetic targetPos) {
        Length slantRange = radarPos.getDistance(targetPos);
        Angle azimuth = radarPos.getAzimuth(targetPos);
        Length targetAlt = targetPos.alt();
        return new Observation(azimuth, slantRange, targetAlt);
    }

    /**
     * Converts a radar detection in spherical coordinates (relative to the radar)
     * to a geodetic position on Earth, accounting for Earth curvature and a
     * specified atmospheric refraction factor.
     *
     * @param radarPos    The geodetic position of the radar.
     * @param targetSph   The target's spherical coordinates (az, el, range) relative to the radar.
     * @param kFactor     The atmospheric refraction k-factor (e.g., 4/3 for standard refraction).
     * @return The calculated geodetic position of the target.
     */ // NOSONAR
    public static Geodetic toGeodetic(Geodetic radarPos, Spherical targetSph, double kFactor) {
        // 1. Get key parameters from inputs
        Length radarAlt = radarPos.alt();
        Angle radarLat = radarPos.lat();
        Length slantRange = targetSph.range();
        Angle elevation = targetSph.elevation();
        Angle azimuth = targetSph.azimuth();

        // 2. Calculate effective Earth radius and solve for target altitude and central angle
        Length effectiveRadius = IEarthModel.EARTH_MODEL.getEffectiveEarthRadius(radarLat, kFactor);
        RefractionTriangleResult result = solveRefractionTriangle(effectiveRadius, radarAlt, null, slantRange, elevation);
        Length targetAlt = result.targetAlt();
        double gamma = result.centralAngleRad();

        // 5. Calculate the ground distance (arc length)
        Length groundDistance = effectiveRadius.multiply(gamma); // NOSONAR

        // 6. Solve the direct geodetic problem: given a start point, bearing, and distance, find the end point.
        // This uses Vincenty's direct formula for high accuracy.
        double lat1 = radarLat.inRadians();
        double lon1 = radarPos.lon().inRadians();
        double alpha1 = azimuth.inRadians();
        double s = groundDistance.getBase();
        // 4. Solve the direct geodetic problem: given a start point, bearing, and distance, find the end point.
        double sinAlpha1 = Math.sin(alpha1);
        double cosAlpha1 = Math.cos(alpha1);

        double f = 1.0 / 298.257223563; // WGS84 flattening
        double b = Earth84.INSTANCE.getEarthRadius(Angle.fromDegree(90)).getBase(); // Semi-minor axis // NOSONAR

        double tanU1 = (1 - f) * Math.tan(lat1);
        double cosU1 = 1 / Math.sqrt((1 + tanU1 * tanU1));
        double sinU1 = tanU1 * cosU1;
        double sigma1 = Math.atan2(tanU1, cosAlpha1);
        double sinAlpha = cosU1 * sinAlpha1;
        double cosSqAlpha = 1 - sinAlpha * sinAlpha;
        double uSq = cosSqAlpha * (Earth84.A * Earth84.A - b * b) / (b * b);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));

        double sigma = s / (b * A);
        double sigmaP;
        double cos2SigmaM;
        double sinSigma;
        int iterLimit = 100;
        do {
            cos2SigmaM = Math.cos(2 * sigma1 + sigma);
            sinSigma = Math.sin(sigma);
            double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (Math.cos(sigma) * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
            sigmaP = sigma;
            sigma = s / (b * A) + deltaSigma;
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
            lat2 = Math.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1, (1 - f) * Math.sqrt(sinAlpha * sinAlpha + tmp * tmp));
            double lambda = Math.atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1);
            double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
            double L = lambda - (1 - C) * f * sinAlpha * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
            lon2 = lon1 + L;
        } else {
            // Fallback to Spherical model if Vincenty's formula fails to converge
            double R = EarthSph.INSTANCE.getEarthRadius(radarLat).getBase();
            double angDist = s / R;
            lat2 = Math.asin(Math.sin(lat1) * Math.cos(angDist) + Math.cos(lat1) * Math.sin(angDist) * cosAlpha1);
            lon2 = lon1 + Math.atan2(sinAlpha1 * Math.sin(angDist) * Math.cos(lat1), Math.cos(angDist) - Math.sin(lat1) * Math.sin(lat2));
        }

        return new Geodetic(Angle.fromLongitudeRad(lon2), Angle.fromLatitudeRad(lat2), targetAlt); // NOSONAR
    }

    /**
     * A record to hold the results of the {@code solveRefractionTriangle} calculation.
     */
    private record RefractionTriangleResult(Length targetAlt, Angle elevation, double centralAngleRad) {}

    /**
     * Solves the geometry of the triangle formed by the Earth's center, the radar, and the target.
     * This is the core geometric calculation shared by {@code toGeodetic} and {@code toSpherical}.
     * <p>
     * The method uses the law of cosines. It can solve for:
     * <ul>
     *     <li>Target altitude and central angle, given elevation.</li>
     *     <li>Elevation and central angle, given target altitude.</li>
     * </ul>
     *
     * @param effectiveRadius The effective Earth radius (Re').
     * @param radarAlt The radar's altitude (h_r).
     * @param targetAlt The target's altitude (h_t), or null if unknown.
     * @param slantRange The slant range (chord distance) between radar and target (R_s).
     * @param elevation The elevation angle (beta), or null if unknown.
     * @return A {@link RefractionTriangleResult} containing the calculated values.
     */
    private static RefractionTriangleResult solveRefractionTriangle(
            Length effectiveRadius, Length radarAlt, Length targetAlt,
            Length slantRange, Angle elevation) {

        double Re = effectiveRadius.getBase();
        double h_r = radarAlt.getBase();
        double R_s = slantRange.getBase();

        Length finalTargetAlt = targetAlt;
        Angle finalElevation = elevation;
        double centralAngleRad;

        // Side lengths of the triangle from Earth's center
        double side_C_R = Re + h_r; // Earth Center to Radar

        if (elevation != null) { // Solving for target altitude (used in toGeodetic)
            double elRad = elevation.inRadians();
            // Law of cosines to find side_C_T^2
            double side_C_T_sq = side_C_R * side_C_R + R_s * R_s + 2 * side_C_R * R_s * Math.sin(elRad);
            double side_C_T = Math.sqrt(side_C_T_sq);
            finalTargetAlt = Length.fromAltitudeMeter(side_C_T - Re);

            // Law of cosines to find central angle (gamma)
            double cosGamma = (side_C_R * side_C_R + side_C_T_sq - R_s * R_s) / (2 * side_C_R * side_C_T);
            centralAngleRad = Math.acos(Math.max(-1.0, Math.min(1.0, cosGamma)));

        } else if (targetAlt != null) { // Solving for elevation (used in toSpherical)
            double h_t = targetAlt.getBase();
            double side_C_T = Re + h_t; // Earth Center to Target

            // Law of cosines to find sin(beta)
            double sinBeta = (side_C_T * side_C_T - side_C_R * side_C_R - R_s * R_s) / (2 * side_C_R * R_s);
            finalElevation = Angle.fromElevation(Math.asin(Math.max(-1.0, Math.min(1.0, sinBeta))), Angle.Unit.RADIAN);

            // Central angle is not strictly needed for toSpherical, but we can calculate it for completeness
            double cosGamma = (side_C_R * side_C_R + side_C_T * side_C_T - R_s * R_s) / (2 * side_C_R * side_C_T);
            centralAngleRad = Math.acos(Math.max(-1.0, Math.min(1.e-12, cosGamma)));
        } else {
            throw new IllegalArgumentException("Either target altitude or elevation must be provided.");
        }

        return new RefractionTriangleResult(finalTargetAlt, finalElevation, centralAngleRad);
    }

    /**
     * Converts a target's geodetic position to spherical coordinates (az, el, range)
     * relative to a radar's geodetic position, accounting for atmospheric refraction
     * calculated from provided atmospheric profiles at both radar and target.
     *
     * @param radarPos The geodetic position of the radar.
     * @param radarPressure Atmospheric pressure at the radar site.
     * @param radarTemp Atmospheric temperature at the radar site.
     * @param radarRH Relative humidity at the radar site (0-100%).
     * @param targetPos The geodetic position of the target.
     * @param targetPressure Atmospheric pressure at the target position.
     * @param targetTemp Atmospheric temperature at the target position.
     * @param targetRH Relative humidity at the target position (0-100%).
     * @return The target's apparent spherical coordinates relative to the radar.
     */
    public static Spherical toSpherical(
            Geodetic radarPos, Pressure radarPressure, Temperature radarTemp, double radarRH,
            Geodetic targetPos, Pressure targetPressure, Temperature targetTemp, double targetRH) {

        double kFactor = Refraction.calculateKFactorFromAtmosphericProfile(
                radarPos.alt(), radarPressure, radarTemp, radarRH,
                targetPos.alt(), targetPressure, targetTemp, targetRH);

        return toSpherical(radarPos, targetPos, kFactor);
    }

    /**
     * Converts a radar detection in spherical coordinates to a geodetic position,
     * using an iterative solver to determine the atmospheric refraction k-factor.
     * <p>
     * This method is used when the k-factor is not constant but depends on the
     * atmospheric profile between the radar and the target. Since the target's
     * altitude is unknown, the method starts with a standard k-factor, calculates
     * an estimated target position, and uses that position's altitude to refine
     * the k-factor. This process repeats until the k-factor converges.
     *
     * @param radarPos The geodetic position of the radar.
     * @param radarPressure Atmospheric pressure at the radar site.
     * @param radarTemp Atmospheric temperature at the radar site.
     * @param radarRH Relative humidity at the radar site (0-100%).
     * @param targetSph The target's spherical coordinates (az, el, range) relative to the radar.
     * @return The calculated geodetic position of the target.
     */
    public static Geodetic toGeodetic(
            Geodetic radarPos, Pressure radarPressure, Temperature radarTemp, double radarRH,
            Spherical targetSph) {

        final int MAX_ITERATIONS = 10;
        final double K_TOLERANCE = 1e-6;

        double kFactor = STANDARD_REFRACTION_K; // Initial guess
        Geodetic estimatedTargetPos = null;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            // 1. Calculate target position with the current k-factor
            estimatedTargetPos = toGeodetic(radarPos, targetSph, kFactor);
            Length estimatedTargetAlt = estimatedTargetPos.alt();

            // 2. Recalculate k-factor using the new estimated target altitude
            double newKFactor = Refraction.calculateKFactorFromAtmosphericProfile(
                    radarPos.alt(), radarPressure, radarTemp, radarRH, estimatedTargetAlt);

            // 3. Check for convergence
            if (Math.abs(newKFactor - kFactor) < K_TOLERANCE) {
                break; // Converged
            }
            kFactor = newKFactor; // Update for next iteration
        }
        return estimatedTargetPos; // Return the last calculated position
    }

    /**
     * Converts a list of radar detections in spherical coordinates (relative to the radar)
     * to geodetic positions on Earth, accounting for Earth curvature and
     * standard atmospheric refraction (k=4/3).
     *
     * @param radarPos The geodetic position of the radar.
     * @param targetSphericals A list of the target's spherical coordinates (az, el, range) relative to the radar.
     * @return A list of the calculated geodetic positions of the targets.
     */
    public static List<Geodetic> toGeodetics(Geodetic radarPos, List<Spherical> targetSphericals) {
        // Default to standard atmospheric refraction
        return toGeodetics(radarPos, targetSphericals, STANDARD_REFRACTION_K);
    }

    /**
     * Converts a list of radar detections in spherical coordinates (relative to the radar)
     * to geodetic positions on Earth, accounting for Earth curvature and a
     * specified atmospheric refraction factor.
     *
     * @param radarPos The geodetic position of the radar.
     * @param targetSphericals A list of the target's spherical coordinates (az, el, range) relative to the radar.
     * @param kFactor The atmospheric refraction k-factor (e.g., 4/3 for standard refraction).
     * @return A list of the calculated geodetic positions of the targets.
     */
    public static List<Geodetic> toGeodetics(Geodetic radarPos, List<Spherical> targetSphericals, double kFactor) {
        if (targetSphericals == null) {
            throw new IllegalArgumentException("Target sphericals list cannot be null.");
        }
        return targetSphericals.stream()
                .map(targetSph -> toGeodetic(radarPos, targetSph, kFactor))
                .collect(Collectors.toList());
    }


    /**
     * Calculates the radar horizon distance for a given radar altitude, accounting for
     * standard atmospheric refraction (k=4/3).
     *
     * @param radarAltitude The altitude of the radar antenna above the Earth's surface.
     * @param radarLatitude The geodetic latitude of the radar, used for precise radius calculation.
     * @return The distance to the radar horizon as a Length quantity.
     */ // NOSONAR
    public static Length getHorizonDistance(Length radarAltitude, Angle radarLatitude) {
        return getHorizonDistance(radarAltitude, radarLatitude, STANDARD_REFRACTION_K);
    }

    /**
     * Calculates the radar horizon distance for a given radar altitude and a specific
     * atmospheric refraction k-factor. The horizon is the geometric line-of-sight
     * distance from the radar to the Earth's surface, accounting for curvature.
     *
     * @param radarAltitude The altitude of the radar antenna above the Earth's surface.
     * @param radarLatitude The geodetic latitude of the radar, used for precise radius calculation.
     * @param kFactor       The atmospheric refraction k-factor (e.g., 4/3 for standard refraction).
     * @return The distance to the radar horizon as a Length quantity.
     */ // NOSONAR
    public static Length getHorizonDistance(Length radarAltitude, Angle radarLatitude, double kFactor) {
        // 1. Calculate the effective Earth radius at the radar's location
        Length effectiveRadius = IEarthModel.EARTH_MODEL.getEffectiveEarthRadius(radarLatitude, kFactor);

        // 2. Get parameters in base units (meters)
        double Re = effectiveRadius.getBase();
        double h = radarAltitude.getBase();

        // 3. Calculate horizon distance using the geometric formula: d = sqrt(2*Re*h + h^2)
        // This is derived from the right triangle formed by the Earth's center, the radar, and the horizon point.
        double horizonDistSq = (2 * Re * h) + (h * h);
        double horizonDist = Math.sqrt(horizonDistSq);

        return Length.fromDistanceMeter(horizonDist);
    }
}
