package io.github.gcng54.flyeval.lib.utils;

public class UnitUtils {
    public static final double EPSILON = 1E-12;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    public FlightUtils() {
        throw new UnsupportedOperationException("No instances allowed!");
    }



    /**
     * Converts a string to sentence case (first letter capitalized, rest
     * lowercase).
     *
     * @param input The string to convert.
     * @return The sentence-cased string.
     */
    public static String toSentenceCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                input.substring(1).toLowerCase(Locale.ENGLISH);
    }

    /**
     * Checks if a value is close to zero within a given epsilon.
     *
     * @param value   The value to check.
     * @param epsilon The tolerance.
     * @return True if the absolute value is less than epsilon.
     */
    public static boolean isCloseToZero(double value, double epsilon) {
        return Math.abs(value) < epsilon;
    }

    /**
     * Checks if a value is close to zero using the default {@link #EPSILON}.
     *
     * @param value The value to check.
     * @return True if the absolute value is less than the default epsilon.
     */
    public static boolean isCloseToZero(double value) {
        return isCloseToZero(value, EPSILON);
    }

    /**
     * Clamps the value to be within the min/max bounds with EWrapMode.
     *
     * @param value    The value to wrap.
     * @param min      The minimum bound.
     * @param max      The maximum bound.
     * @param wrapMode The wrapping mode.
     *                 <ul>
     *                 <li>NONE: No wrapping.</li>
     *                 <li>BOUND: Wraps to min/max.</li>
     *                 <li>CYCLE: Wraps around from max to min. Longitude, Azimuth
     *                 uses this.</li>
     *                 <li>BOUNCE: Like cycle but reverses a direction at min and
     *                 max. Latitude uses this.</li>
     *                 </ul>
     * @return The wrapped value.
     *         <h4>Examples:</h4>
     *
     *         <pre>{@code
     * double min = -90.0;
     * double max = 90.0;
     * double value = 100.0;
     *  // NONE: 100.0
     *  double noneWrapped = Utils.wrapWithMode(value, min, max, Utils.EWrapMode.NONE);
     *  // BOUND: 90.0
     * double boundWrapped = Utils.wrapWithMode(value, min, max, Utils.EWrapMode.BOUND);
     *  // CYCLE: -80.0
     * double cycleWrapped = Utils.wrapWithMode(value, min, max, Utils.EWrapMode.CYCLE);
     *  // BOUNCE: 80.0
     * double bounceWrapped = Utils.wrapWithMode(value, min, max, Utils.EWrapMode.BOUNCE);
     * }</pre>
     *
     * @see EWrapMode
     */
    public static double wrapWithMode(double value, double min, double max, EWrapMode wrapMode) {
        validateMinToMax(min, max);
        return switch (wrapMode) {
            case NONE -> value;
            case BOUND -> wrapBound(value, min, max);
            case CYCLE -> wrapCycle(value, min, max);
            case BOUNCE -> wrapBounce(value, min, max);
        };
    }

    /**
     * Checks if a value is within a given range [min, max].
     *
     * @param value The value to check.
     * @param min   The minimum bound.
     * @param max   The maximum bound.
     * @return True if the value is within the range.
     */
    public static boolean isRange(double value, double min, double max) {
        return max > min && value >= min && value <= max;
    }

    /**
     * Clamps a value to be within the min/max bounds.
     *
     * @param value The value to clamp.
     * @param min   The minimum bound.
     * @param max   The maximum bound.
     * @return The clamped value.
     */
    public static double inRange(double value, double min, double max) {
        return max > min ? Math.max(min, Math.min(value, max)) : value;
    }

    /**
     * Validates that the minimum value is not greater than the maximum value.
     *
     * @throws IllegalArgumentException if min > max.
     */
    public static void validateMinToMax(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum value cannot be greater than maximum value.");
        }
    }

    /**
     * Clamps the value to be within the min/max bounds.
     */
    public static double wrapBound(double value, double min, double max) {
        if (isRange(value, min, max))
            return value;
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Wraps the value around the min/max range.
     */
    public static double wrapCycle(double value, double min, double max) {
        if (isRange(value, min, max))
            return value;
        double range = max - min;
        // Avoid infinite loop if range is not positive
        if (range <= 0)
            return value;
        while (value < min)
            value += range;
        while (value > max)
            value -= range;
        return value;
    }

    /**
     * Bounces the value back and forth within the min/max range.
     */
    public static double wrapBounce(double value, double min, double max) {
        if (isRange(value, min, max))
            return value;
        while (value < min || value > max) {
            if (value > max) {
                value = 2 * max - value;
            } else {
                value = 2 * min - value;
            }
        }
        return value;
    }

    /**
     * Calculates the azimuth in radians from the given x and y components.
     * The azimuth is measured clockwise from the north (y-axis).
     * <p>
     * The method uses Math.atan2 to compute the angle, which correctly handles
     * the quadrants and edge cases. The result is normalized to the range (0, 2π).
     *
     * @return The azimuth in radians, in the range [0, 2π).
     *         <h4>Examples:</h4>
     *
     *         <pre>{@code
     * double azimuth1 = getAzimuthInRad(0.0, 1.0); // 0.0 (north)
     * double azimuth2 = getAzimuthInRad(1.0, 0.0); // π/2 (east)
     * double azimuth3 = getAzimuthInRad(0.0, -1.0); // π (south)
     * double azimuth4 = getAzimuthInRad(-1.0, 0.0); // 3π/2 (west)
     * }</pre>
     */
    public static double getAzimuthInRad(double x, double y) {
        // If both components are zero, return 0 by convention (undefined direction)
        if (x == 0.0 && y == 0.0) {
            return 0.0;
        }
        // atan2 returns 0 for (0, 1) (north), which is correct for azimuth
        double radian = Math.atan2(x, y); // returns [-PI, PI], 0 = north, PI/2 = east
        if (radian < 0.0)
            radian += 2.0 * Math.PI; // normalize to [0, 2PI)
        // If an angle is extremely close to 0 or 2*PI, snap to 0.0 to avoid -0.0 or
        // floating-point noise
        if (Math.abs(radian) < EPSILON || Math.abs(radian - 2.0 * Math.PI) < EPSILON)
            radian = 0.0;
        return radian;
    }

    /**
     * Calculates the speed of sound (Mach 1) for a given altitude using the
     * International Standard Atmosphere (ISA) model.
     *
     * @param altitude_meter The geometric altitude.
     * @return The speed of sound as a Speed quantity in m/s.
     */
    public static double getSpeedOfSoundInMPS(double altitude_meter) {
        // ISA constants
        final double T0 = 288.15; // Sea level standard temperature in Kelvin (15°C)
        final double GAMMA = 1.4; // Specific heat ratio for air
        final double R = 287.058; // Specific gas constant for dry air (J/(kg·K))
        final double LAPSE_RATE_TROPO = -0.0065; // Temperature lapse rate in the Troposphere (K/m)

        double temperature;

        if (altitude_meter <= 11000) { // Troposphere
            temperature = T0 + LAPSE_RATE_TROPO * altitude_meter;
        } else {
            temperature = 216.65; // Constant temperature
        }
        return Math.sqrt(GAMMA * R * temperature);
    }

    /**
     * Modes for wrapping values within a range.
     * <p>
     * <ul>
     * <li>NONE: No wrapping.</li>
     * <li>BOUND: Wraps to min/max.</li>
     * <li>CYCLE: Wraps around from max to min. Longitude, Azimuth uses this.</li>
     * <li>BOUNCE: Like cycle but reverses a direction at min and max. Latitude uses
     * this.</li>
     * </ul>
     */
    public enum EWrapMode {
        NONE,
        BOUND,
        CYCLE,
        BOUNCE,
    }

}
