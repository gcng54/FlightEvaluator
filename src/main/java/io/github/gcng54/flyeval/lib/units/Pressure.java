package io.github.gcng54.flyeval.lib.units;

import java.util.Locale;

/** Quantity representing pressure. */
public class Pressure extends AQuantity<Pressure, Pressure.Unit> {

    /**
     * Defines standard units of pressure. The base unit is {@link #PASCAL}.
     */
    public enum Unit implements IUnit<Pressure.Unit> {
        PASCAL("Pa", 1.0),
        HECTOPASCAL("hPa", 100.0),
        KILOPASCAL("kPa", 1000.0),
        BAR("bar", 100_000.0),
        MILLIBAR("mbar", 100.0), // Same as hPa
        PSI("psi", 6894.75729); // 1 POUNDS_PER_SQ_INCH = 6894.75729 Pa

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
         * @return The corresponding Pressure.Unit constant.
         */
        public static Pressure.Unit fromName(String name_) {
            return Pressure.Unit.valueOf(name_.toUpperCase(Locale.ENGLISH));
        }

        @Override
        public String getSymbol() {
            return symbol;
        }

        @Override
        public double getFactor() {
            return factor;
        }

        @Override
        public String toString() {
            return switch (this) {
                case PSI -> "PSI";

                default -> IUnit.toSentenceCase(this.name()) + "s";
            };
        }

    }

    /** @return value expressed in pascals. */
    public double inPascal() {
        return this.inUnit(Pressure.Unit.PASCAL);
    }

    /** @return value expressed in millibars. */
    public double inMillibar() {
        return this.inUnit(Pressure.Unit.MILLIBAR);
    }

    public static Pressure fromPressure(double val, Pressure.Unit unit) {
        return new Pressure(val, unit).wrapPositive();
    }

    public static Pressure fromAirPressure(double val, Pressure.Unit unit) {
        return new Pressure(val, unit).wrapPositive();
    }

    public static Pressure fromPascal(double pascals) {
        return new Pressure(pascals, Pressure.Unit.PASCAL).wrapPositive();
    }
    
    public static Pressure fromBar(double bars) {
        return new Pressure(bars, Pressure.Unit.BAR).wrapPositive();
    }

    public static Pressure fromMillibar(double millibars) {
        return new Pressure(millibars, Pressure.Unit.MILLIBAR).wrapPositive();
    }

    public static Pressure fromHectopascal(double hectopascal) {
        return new Pressure(hectopascal, Pressure.Unit.HECTOPASCAL).wrapPositive();
    }

    public Pressure(double value, Pressure.Unit unit) {
        super(value, unit);
    }

    @Override
    public Pressure create(double val, Pressure.Unit u) {
        return new Pressure(val, u);
    }
}
