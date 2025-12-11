package io.github.gcng54.flyeval.lib.units;

public record Wrapper(double minValue, double maxValue, EWrapMode wrapMode) {

    public Wrapper(double minValue, double maxValue){
        this(minValue, maxValue, EWrapMode.BOUND);
    }

    public Wrapper(double maxValue){
        this(0.0, maxValue, EWrapMode.BOUND);
    }

    public Wrapper {
        validateMinToMax (minValue, maxValue) ;
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
     *  double noneWrapped = wrapWithMode(value, min, max, Wrapper.EWrapMode.NONE);
     *  // BOUND: 90.0
     * double boundWrapped = wrapWithMode(value, min, max, Wrapper.EWrapMode.BOUND);
     *  // CYCLE: -80.0
     * double cycleWrapped = wrapWithMode(value, min, max, Wrapper.EWrapMode.CYCLE);
     *  // BOUNCE: 80.0
     * double bounceWrapped = wrapWithMode(value, min, max, Wrapper.EWrapMode.BOUNCE);
     * }</pre>
     *
     * @see EWrapMode
     */
    public static double wrap(double value, double min, double max, EWrapMode wrapMode) {
        validateMinToMax(min, max);
        return switch (wrapMode) {
            case NONE -> value;
            case BOUND -> wrapBound(value, min, max);
            case CYCLE -> wrapCycle(value, min, max);
            case BOUNCE -> wrapBounce(value, min, max);
        };
    }

    public double wrap(double value, EWrapMode wrapMode) {
        return wrap(value, this.minValue(), this.maxValue(), wrapMode);
    }

    public double wrap(double value) {
        return wrap(value, this.minValue(), this.maxValue(), this.wrapMode());
    }

    public double wrap(double value, IUnit<?> unit) {
        double wrapbase = wrap(value, this.minValue(), this.maxValue(), this.wrapMode());
        return unit.fromBase(wrapbase);
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

    public boolean isRange(double value){
        return isRange(value, this.minValue(), this.maxValue());
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

    public double inRange(double value){
        return inRange(value, this.minValue(), this.maxValue());
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

    public double wrapBound(double value){
        return wrapBound(value, this.minValue(), this.maxValue());
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

    public double wrapCycle(double value){
        return wrapCycle(value, this.minValue(), this.maxValue());
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
    public double wrapBounce(double value){
        return wrapBounce(value, this.minValue(), this.maxValue());
    }
}
