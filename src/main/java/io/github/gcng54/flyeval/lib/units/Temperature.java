package io.github.gcng54.flyeval.lib.units;

import java.util.Locale;

/** Quantity representing temperature. */
public class Temperature extends AQuantity<Temperature, Temperature.Unit> {

    /**
     * Defines standard units of temperature. The base unit is {@link #KELVIN}.
     * <p>
     * Note: Temperature conversions are affine, not linear, so they override the
     * default {@code toBase} and {@code fromBase} methods.
     */
    public enum Unit implements IUnit<Unit> {
        KELVIN("K", 1.0),
        CELSIUS("°C", 1.0),
        FAHRENHEIT("°F", 1.0); // Factor is not used for conversion but required by interface

        private final String symbol;
        private final double factor;

        Unit(String symbol, double factor) {
            validateFactor(factor);
            this.symbol = symbol;
            this.factor = factor;
        }

        /**
         * Gets the enum constant from its string name (case-insensitive).
         * 
         * @param name_ The name of the enum constant.
         * @return The corresponding ETemperatures constant.
         */
        public static Unit fromName(String name_) {
            return Unit.valueOf(name_.toUpperCase(Locale.ENGLISH));
        }

        @Override
        public String getSymbol() {
            return symbol;
        }

        @Override
        public double getFactor() {
            return factor; // This is a dummy for non-affine units, real logic is in to/fromBase.
        } // This is a dummy, real logic is in Temperature class

        @Override
        public String toString() {
            return IUnit.toSentenceCase(this.name());
        }

        /**
         * Converts a value from this unit to the base unit (Kelvin).
         * 
         * @param value The temperature value in this unit.
         * @return The temperature value in Kelvin.
         */
        @Override
        public double toBase(double value) {
            return switch (this) {
                case KELVIN -> value;
                case CELSIUS -> value + 273.15;
                case FAHRENHEIT -> (value - 32) * 5.0 / 9.0 + 273.15;
            };
        }

        /**
         * Converts a value from the base unit (Kelvin) to this unit.
         * 
         * @param baseValue The temperature value in Kelvin.
         * @return The temperature value in this unit.
         */
        @Override
        public double fromBase(double baseValue) {
            return switch (this) {
                case KELVIN -> baseValue;
                case CELSIUS -> baseValue - 273.15;
                case FAHRENHEIT -> (baseValue - 273.15) * 9.0 / 5.0 + 32;
            };
        }
    }

    public Temperature(double value, Temperature.Unit unit) {
        super(value, unit);
    }

    @Override
    public Temperature create(double val, Temperature.Unit unit) {
        return new Temperature(val, unit);
    }

    @Override
    public double inUnit(Temperature.Unit targetUnit) {
        return targetUnit.fromBase(this.toBase(this.getValue()));
    }

    @Override
    public double toBase(double value) {
        return switch (this.unit) {
            case KELVIN -> value;
            case CELSIUS -> value + 273.15;
            case FAHRENHEIT -> (value - 32) * 5.0 / 9.0 + 273.15;
        };
    }

    @Override
    public double fromBase(double baseValue) {
        return switch (this.unit) {
            case KELVIN -> baseValue;
            case CELSIUS -> baseValue - 273.15;
            case FAHRENHEIT -> (baseValue - 273.15) * 9.0 / 5.0 + 32;
        };
    }

    /** @return value expressed in celsius. */
    public double inCelsius() {
        return this.inUnit(Temperature.Unit.CELSIUS);
    }

    /** @return value expressed in kelvin. */
    public double inKelvin() {
        return this.inUnit(Temperature.Unit.KELVIN);
    }

    public static Temperature ofTemp(double val, Temperature.Unit unit) {
        return new Temperature(val, unit);
    }

    public static Temperature ofCelsius(double celsius) {
        return new Temperature(celsius, Temperature.Unit.CELSIUS)
                .wrapBound(Temperature.Unit.CELSIUS.toBase(-273.15), Double.MAX_VALUE);
    }
    
    public static Temperature ofKelvin(double kelvin) {
        return new Temperature(kelvin, Temperature.Unit.KELVIN).wrapPositive();
    }

    public static Temperature ofFahrenheit(double fahrenheit) {
        return new Temperature(fahrenheit, Temperature.Unit.FAHRENHEIT)
                .wrapBound(Temperature.Unit.FAHRENHEIT.toBase(-459.67), Double.MAX_VALUE);
    }
}
