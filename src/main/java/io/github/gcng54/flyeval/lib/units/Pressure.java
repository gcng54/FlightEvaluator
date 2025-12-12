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
        MILLIBAR("mbar", 100.0), // 1 millibar = 100 pascals
        MMHG("mmHg", 133.322368), // 1 mmHg = 133.322368 Pa
        INHG("inHg", 3386.389), // 1 inHg = 3386.389 Pa
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

    /** @return value expressed in millibars. 1 millibar = 100 pascals */
    public double inMillibar() {
        return this.inUnit(Pressure.Unit.MILLIBAR);
    }
    /** @return value expressed in bars. */
    public double inBar() {
        return this.inUnit(Pressure.Unit.BAR);
    }
    /** @return value expressed in hectopascals. */
    public double inHectopascal() {
        return this.inUnit(Pressure.Unit.HECTOPASCAL);
    }
    /** @return value expressed in mmHg. 1 mmHg = 133.322368 Pa */
    public double inMMHG() {
        return this.inUnit(Pressure.Unit.MMHG);
    }
    /** @return value expressed in inHg. 1 inHg = 3386.389 Pa*/
    public double inINHG() {
        return this.inUnit(Pressure.Unit.INHG);
    }
    /** @return value expressed in psi. 1 POUNDS_PER_SQ_INCH = 6894.75729 Pa */
    public double inPSI() {
        return this.inUnit(Pressure.Unit.PSI);
    }

    public static Pressure ofAirPressure(double val, Pressure.Unit unit) {
        return new Pressure(val, unit).wrapPositive();
    }

    public static Pressure ofPascal(double pascals) {
        return new Pressure(pascals, Pressure.Unit.PASCAL).wrapPositive();
    }
    
    public static Pressure ofBar(double bars) {
        return new Pressure(bars, Pressure.Unit.BAR).wrapPositive();
    }

    public static Pressure ofMillibar(double millibars) {
        return new Pressure(millibars, Pressure.Unit.MILLIBAR).wrapPositive();
    }

    public static Pressure ofHectopascal(double hectopascal) {
        return new Pressure(hectopascal, Pressure.Unit.HECTOPASCAL).wrapPositive();
    }

    public static Pressure ofMMHG(double mmHg) {
        return new Pressure(mmHg, Pressure.Unit.MMHG).wrapPositive();
    }

    public static Pressure ofINHG(double inHg) {
        return new Pressure(inHg, Pressure.Unit.INHG).wrapPositive();
    }

    public Pressure(double value, Pressure.Unit unit) {
        super(value, unit);
    }

    @Override
    public Pressure create(double val, Pressure.Unit u) {
        return new Pressure(val, u);
    }
}
