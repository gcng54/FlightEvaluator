package io.github.gcng54.flyeval.lib.radars;

import io.github.gcng54.flyeval.lib.units.*;

/**
 * Provides utility methods for calculating atmospheric refraction properties.
 */
public class Refraction {

    // ISA constants
    private static final double ISA_T0 = 288.15; // Sea level standard temperature in Kelvin (15Â°C)
    private static final double ISA_P0 = 101325.0; // Sea level standard pressure in Pascals
    private static final double ISA_G = 9.80665; // Gravitational acceleration (m/s^2)
    private static final double ISA_R = 287.058; // Specific gas constant for dry air (J/(kgÂ·K))
    private static final double ISA_LAPSE_RATE_TROPO = -0.0065; // Temperature lapse rate in the Troposphere (K/m)
    private static final double ISA_TROPOPAUSE_ALT = 11000.0; // Tropopause altitude (m)
    private static final double ISA_TROPOPAUSE_TEMP = ISA_T0 + ISA_LAPSE_RATE_TROPO * ISA_TROPOPAUSE_ALT; // Temperature at tropopause

    // --- Standard Atmosphere Model ---

    /**
     * Calculates the Saturation Vapor Pressure (es) using the Tetens equation.
     * This is an intermediate step required to find the actual Water Vapor Pressure.
     *
     * @param temp Temperature.
     * @return Saturation Vapor Pressure (es) in hPa (hectopascals).
     */
    public static Temperature getStandardTemperature(Length altitude) {
        double h_m = altitude.inMeter();
        double tempK;

        if (h_m <= ISA_TROPOPAUSE_ALT) { // Troposphere
            tempK = ISA_T0 + ISA_LAPSE_RATE_TROPO * h_m;
        } else { // Stratosphere (constant temperature up to 20km, then other layers)
            // For simplicity, we'll assume constant temp above tropopause for this range.
            // A full ISA model would have more layers.
            tempK = ISA_TROPOPAUSE_TEMP;
        }
        return Temperature.fromKelvin(tempK);
    }

    /**
     * Calculates the standard atmospheric pressure at a given altitude using the ISA model.
     *
     * @param altitude The altitude.
     * @return The pressure as a Quants.Pressure quantity in Pascals.
     */
    public static Pressure getStandardPressure(Length altitude) {
        double h_m = altitude.inMeter();
        double pressurePa;

        if (h_m <= ISA_TROPOPAUSE_ALT) { // Troposphere
            Temperature temp = getStandardTemperature(altitude);
            double tempK = temp.inKelvin();
            pressurePa = ISA_P0 * Math.pow(tempK / ISA_T0, -ISA_G / (ISA_LAPSE_RATE_TROPO * ISA_R));
        } else { // Stratosphere (constant temperature)
            // Pressure at tropopause
            Pressure p_tropopause = getStandardPressure(Length.fromMeter(ISA_TROPOPAUSE_ALT));
            double p_tropopause_Pa = p_tropopause.inUnit(Pressure.Unit.PASCAL);
            double tempK = ISA_TROPOPAUSE_TEMP; // Constant temperature in this layer
            pressurePa = p_tropopause_Pa * Math.exp(-ISA_G * (h_m - ISA_TROPOPAUSE_ALT) / (ISA_R * tempK));
        }
        return Pressure.fromPascal(pressurePa);
    }

    /**
     * Assumes a standard relative humidity for atmospheric calculations.
     * This is a simplification; actual humidity varies greatly.
     * For this standard atmosphere, we assume a constant 60% relative humidity.
     *
     * @param altitude The altitude (currently unused, as RH is constant in this simplified model).
     * @return The assumed standard relative humidity (0-100%).
     */
    public static double getStandardRelativeHumidity(Length altitude) {
        // For a simplified standard atmosphere, we can assume a constant RH.
        // In reality, RH varies significantly with altitude and weather.
        return 60.0; // 60% relative humidity
    }

    // --- Refractivity Calculations ---

    /**
     * Calculates the Saturation Vapor Pressure (es) using the Tetens equation.
     * This is an intermediate step required to find the actual Water Vapor Pressure.
     *
     * @param temp Temperature.
     * @return Saturation Vapor Pressure (es) in hPa (hectopascals).
     */
    public static Pressure calculateSaturationVaporPressure(Temperature temp) {
        double tempCelsius = temp.inCelsius();
        // Tetens formula constants for water (over ice constants differ slightly)
        final double A = 6.112;
        final double B = 17.67;
        final double C = 243.5;

        double es_hpa = A * Math.exp((B * tempCelsius) / (tempCelsius + C));
        // Convert hPa to Pa for Quants.Pressure base unit
        return Pressure.fromPascal(es_hpa * 100);
    }

    /**
     * Calculates the actual Partial Water Vapor Pressure (e).
     *
     * @param temp Temperature.
     * @param relativeHumidity Relative humidity in percent (0-100).
     * @return Partial Water Vapor Pressure (e) in hPa.
     */
    public static Pressure calculateVaporPressure(Temperature temp, double relativeHumidity) {
        if (relativeHumidity < 0 || relativeHumidity > 100) {
            throw new IllegalArgumentException("Relative humidity must be between 0 and 100 percent.");
        }
        Pressure es = calculateSaturationVaporPressure(temp);
        double e_hpa = es.inUnit(Pressure.Unit.HECTOPASCAL) * (relativeHumidity / 100.0);
        // Convert hPa to Pa for Quants.Pressure base unit
        return Pressure.fromPascal(e_hpa * 100);
    }

    /**
     * Calculates Radar Refractivity (N) in N-units.
     * This is the standard "NS" (Surface Refractivity) value if input data is from surface level.
     * Formula based on ITU-R P.453.
     *
     * @param pressure Total atmospheric pressure.
     * @param temp Temperature.
     * @param relativeHumidity Relative humidity in percent (0-100).
     * @return Refractivity (N) in N-units (dimensionless).
     */
    public static double calculateRefractivity(Pressure pressure, Temperature temp, double relativeHumidity) {
        if (relativeHumidity < 0 || relativeHumidity > 100) {
            throw new IllegalArgumentException("Relative humidity must be between 0 and 100 percent.");
        }
        double pressureHpa = pressure.inUnit(Pressure.Unit.HECTOPASCAL);
        double tempKelvin = temp.inKelvin();
        Pressure vaporPressure = calculateVaporPressure(temp, relativeHumidity);
        double e_hpa = vaporPressure.inUnit(Pressure.Unit.HECTOPASCAL);

        // Term 1: Dry term (77.6 * P / T)
        double dryTerm = 77.6 * (pressureHpa / tempKelvin);

        // Term 2: Wet term (3.73 * 10^5 * e / T^2)
        double wetTerm = 3.732e5 * (e_hpa / (tempKelvin * tempKelvin));

        return dryTerm + wetTerm;
    }

    /**
     * Calculates the Refractive Index (n) from Refractivity (N).
     *
     * @param N Refractivity in N-units.
     * @return Refractive Index (n).
     */
    public static double calculateRefractiveIndex(double N) {
        return 1 + (N * 1e-6);
    }
    
    /**
     * Calculates Modified Refractivity (M).
     * M-units are often used in radar coverage diagrams to account for Earth's curvature.
     *
     * @param N Refractivity in N-units.
     * @param height Height of the receiver/measure point.
     * @return Modified Refractivity (M) in M-units.
     */
    public static double calculateModifiedRefractivity(double N, Length height) {
        double heightMeters = height.inMeter();

        return N + (heightMeters / Radiations.EARTH_RADIUS.inMeter()) * 1e6;
    }

    /**
     * Calculates the average Modified Refractivity (M) between two points along a path.
     * This method assumes that the Refractivity (N) at each point is already known.
     *
     * @param N_site Refractivity (N-units) at the site height.
     * @param siteHeight Height of the site.
     * @param N_target Refractivity (N-units) at the target height.
     * @param targetHeight Height of the target.
     * @return The average Modified Refractivity (M-units) between the two points.
     */
    public static double averageModifiedRefractivity(double N_site, Length siteHeight, double N_target, Length targetHeight) {
        double M_site = calculateModifiedRefractivity(N_site, siteHeight);
        double M_target = calculateModifiedRefractivity(N_target, targetHeight);
        return (M_site + M_target) / 2.0;
    }

    /**
     * Calculates the average Modified Refractivity (M) between two points along a path,
     * deriving the refractivity (N) from a standard atmospheric profile.
     *
     * @param siteHeight Height of the site.
     * @param targetHeight Height of the target.
     * @return The average Modified Refractivity (M-units) between the two points.
     */
    public static double averageModifiedRefractivity(Length siteHeight, Length targetHeight) {
        // Derive N_site from standard atmosphere
        Pressure p_site = getStandardPressure(siteHeight);
        Temperature t_site = getStandardTemperature(siteHeight);
        double rh_site = getStandardRelativeHumidity(siteHeight);
        double N_site = calculateRefractivity(p_site, t_site, rh_site);

        // Derive N_target from standard atmosphere
        Pressure p_target = getStandardPressure(targetHeight);
        Temperature t_target = getStandardTemperature(targetHeight);
        double rh_target = getStandardRelativeHumidity(targetHeight);
        double N_target = calculateRefractivity(p_target, t_target, rh_target);

        return averageModifiedRefractivity(N_site, siteHeight, N_target, targetHeight);
    }

    /**
     * Calculates the average Modified Refractivity (M) between two points, using
     * specific weather inputs for the site and a standard atmospheric profile for the target.
     *
     * @param siteHeight Height of the site.
     * @param sitePressure Pressure at the site.
     * @param siteTemp Temperature at the site.
     * @param siteRH Relative humidity at the site (0-100%).
     * @param targetHeight Height of the target.
     * @return The average Modified Refractivity (M-units) between the two points.
     */
    public static double averageModifiedRefractivity(Length siteHeight, Pressure sitePressure, Temperature siteTemp, double siteRH, Length targetHeight) {
        // Calculate N_site from provided weather data
        double N_site = calculateRefractivity(sitePressure, siteTemp, siteRH);

        // Derive N_target from standard atmosphere model
        Pressure p_target = getStandardPressure(targetHeight);
        Temperature t_target = getStandardTemperature(targetHeight);
        double rh_target = getStandardRelativeHumidity(targetHeight);
        double N_target = calculateRefractivity(p_target, t_target, rh_target);

        return averageModifiedRefractivity(N_site, siteHeight, N_target, targetHeight);
    }

    /**
     * Calculates an effective k-factor for atmospheric refraction based on atmospheric
     * conditions at two different heights. This k-factor models the bending of radar
     * waves due to the refractivity gradient along the path.
     *
     * The k-factor is derived from the gradient of Modified Refractivity (dM/dh).
     *
     * @param h1 Height of the first point.
     * @param p1 Atmospheric pressure at the first point.
     * @param t1 Atmospheric temperature at the first point.
     * @param rh1 Relative humidity at the first point (0-100%).
     * @param h2 Height of the second point.
     * @param p2 Atmospheric pressure at the second point.
     * @param t2 Atmospheric temperature at the second point.
     * @param rh2 Relative humidity at the second point (0-100%).
     * @return The calculated effective k-factor. Returns a large value (e.g., 1000.0)
     *         if the gradient is near zero, indicating strong ducting or near straight-line propagation.
     * @throws IllegalArgumentException if relative humidity is out of range.
     */
    public static double calculateKFactorFromAtmosphericProfile(
            Length h1, Pressure p1, Temperature t1, double rh1,
            Length h2, Pressure p2, Temperature t2, double rh2) {

        // Calculate refractivity (N) at both heights
        double N1 = calculateRefractivity(p1, t1, rh1);
        double N2 = calculateRefractivity(p2, t2, rh2);

        // Calculate modified refractivity (M) at both heights
        double M1 = calculateModifiedRefractivity(N1, h1);
        double M2 = calculateModifiedRefractivity(N2, h2);

        double h1_m = h1.inMeter();
        double h2_m = h2.inMeter();

        // Calculate the gradient of Modified Refractivity (dM/dh)
        double dM_dh;
        if (Math.abs(h2_m - h1_m) < 1e-6) { // Heights are effectively the same
            // If heights are the same, assume standard gradient for dM/dh
            // Standard dN/dh is approx -39 N-units/km = -0.039 N-units/m
            // dM/dh = dN/dh + (1/Re_true) * 10^6
            // (1/Re_true) * 10^6 = (1 / 6371008.8) * 10^6 approx 0.157 N-units/m
            // So, dM/dh approx -0.039 + 0.157 = 0.118 N-units/m
            dM_dh = 0.118; // Standard atmosphere dM/dh in N-units/m
        } else {
            dM_dh = (M2 - M1) / (h2_m - h1_m);
        }

        // Calculate k-factor from the gradient of Modified Refractivity
        // k = (10^6 / R_e_true) / (dM/dh)
        double Re_true = Radiations.EARTH_RADIUS.inMeter();
        if (Math.abs(dM_dh) < 1e-9) { // Avoid division by zero if gradient is near zero
            // If dM/dh is near zero, it implies a very large k-factor (or infinite for ducting)
            return 1000.0; // Represents strong ducting or near-zero gradient
        }
        return (1e6 / Re_true) / dM_dh;
    }

    /**
     * Calculates an effective k-factor for atmospheric refraction based on a standard
     * atmospheric profile between two given heights.
     *
     * @param h1 Height of the first point.
     * @param h2 Height of the second point.
     * @return The calculated effective k-factor from the standard atmosphere.
     */
    public static double calculateKFactorFromStandardAtmosphere(Length h1, Length h2) {
        Pressure p1 = getStandardPressure(h1);
        Temperature t1 = getStandardTemperature(h1);
        double rh1 = getStandardRelativeHumidity(h1);

        Pressure p2 = getStandardPressure(h2);
        Temperature t2 = getStandardTemperature(h2);
        double rh2 = getStandardRelativeHumidity(h2);

        return calculateKFactorFromAtmosphericProfile(h1, p1, t1, rh1, h2, p2, t2, rh2);
    }

    /**
     * Calculates an effective k-factor using specific weather inputs for the first point (site)
     * and a standard atmospheric profile for the second point (target).
     *
     * @param h1 Height of the first point (site).
     * @param p1 Atmospheric pressure at the first point.
     * @param t1 Atmospheric temperature at the first point.
     * @param rh1 Relative humidity at the first point (0-100%).
     * @param h2 Height of the second point (target).
     * @return The calculated effective k-factor.
     */
    public static double calculateKFactorFromAtmosphericProfile(
            Length h1, Pressure p1, Temperature t1, double rh1,
            Length h2) {

        // Get standard atmospheric conditions for the second point
        Pressure p2 = getStandardPressure(h2);
        Temperature t2 = getStandardTemperature(h2);
        double rh2 = getStandardRelativeHumidity(h2);

        return calculateKFactorFromAtmosphericProfile(h1, p1, t1, rh1, h2, p2, t2, rh2);
    }
}


