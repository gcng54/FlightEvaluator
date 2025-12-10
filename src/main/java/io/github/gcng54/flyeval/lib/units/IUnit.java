package io.github.gcng54.flyeval.lib.units;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;

/**
 * A contract for all unit enumerations, providing a standardized way to handle
 * unit conversions and metadata.
 *
 * @param <U> The type of the unit enum itself.
 */
public interface IUnit<U extends IUnit<U>> {
    /**
     * Gets the standard symbol for the unit (e.g., "m", "km/h", "°C").
     *
     * @return The unit symbol string.
     */
    String getSymbol();

    /**
     * Gets the multiplicative factor to convert a value from this unit to its base
     * unit.
     * For affine transformations like temperature, this may be a dummy value.
     *
     * @return The conversion factor.
     */
    double getFactor();

    /**
     * Converts a numeric value from this unit to the system's base unit.
     *
     * @param value The value in the current unit.
     * @return The equivalent value in the base unit.
     */
    default double toBase(double value) {
        return value * this.getFactor();
    }

    /**
     * Converts a numeric value from the system's base unit to this unit.
     *
     * @param baseValue The value in the base unit.
     * @return The equivalent value in the current unit.
     */
    default double fromBase(double baseValue) {
        return baseValue / this.getFactor();
    }

    /**
     * Validates that the conversion factor is a positive number.
     *
     * @param factor The factor to validate.
     * @throws IllegalArgumentException if the factor is not positive.
     */
    default void validateFactor(double factor) {
        if (factor <= 0.0) {
            throw new IllegalArgumentException("Factor must be positive: " + factor);
        }
    }

    default void validateSymbol(String symbol) {
        if (symbol == null || symbol.isEmpty()) {
            throw new IllegalArgumentException("Symbol must be non-empty: " + symbol);
        }
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
     * Converts a value from the current unit to a specified target unit.
     *
     * @param value      The value in the current unit.
     * @param targetUnit The unit to convert to.
     * @return The equivalent value in the target unit.
     */
    default double toUnit(double value, @NotNull IUnit<U> targetUnit) {
        return targetUnit.fromBase(this.toBase(value));
    }

    /**
     * Converts a value from a specified source unit to the current unit.
     *
     * @param value      The value in the source unit.
     * @param sourceUnit The unit to convert from.
     * @return The equivalent value in the current unit.
     */
    default double fromUnit(double value, @NotNull IUnit<U> sourceUnit) {
        return this.fromBase(sourceUnit.toBase(value));
    }

    /**
     * A general-purpose utility to convert a value between any two units of the
     * same type.
     *
     * @param value      The value to convert.
     * @param sourceUnit The unit of the input value.
     * @param targetUnit The desired output unit.
     * @return The converted value.
     */
    default double convert(double value, @NotNull IUnit<U> sourceUnit, @NotNull IUnit<U> targetUnit) {
        if (sourceUnit.equals(targetUnit))
            return value;
        return targetUnit.fromBase(sourceUnit.toBase(value));
    }

    /** Check the same Unit class */
    default boolean isCompatible(@NotNull IUnit<?> targetUnit) {
        return this.getClass().equals(targetUnit.getClass());
    }

    default IUnit<?> getBaseUnit() {
        return getDimension().getBaseUnit();
    }

    default EDimension getDimension() {
        if (this instanceof Length.Unit)
            return EDimension.LENGTH;
        if (this instanceof Angle.Unit)
            return EDimension.ANGLE;
        if (this instanceof Time.Unit)
            return EDimension.TIME;
        if (this instanceof Speed.Unit)
            return EDimension.SPEED;
        if (this instanceof Area.Unit)
            return EDimension.AREA;
        if (this instanceof Volume.Unit)
            return EDimension.VOLUME;
        if (this instanceof Pressure.Unit)
            return EDimension.PRESSURE;
        if (this instanceof Temperature.Unit)
            return EDimension.TEMPERATURE;
        throw new IllegalStateException("Unknown unit dimension for unit: " + this.toString());
    }

}