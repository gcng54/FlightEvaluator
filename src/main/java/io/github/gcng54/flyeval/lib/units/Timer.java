package io.github.gcng54.flyeval.lib.units;

import java.util.Locale;

/** Quantity representing elapsed time. */
public class Timer extends AQuantity<Timer, Timer.Unit> {

public enum Unit implements IUnit<Unit> {
        SECOND("s", 1.0),
        MINUTE("min", 60.0),
        HOUR("h", 3600.0),
        DAY("d", 86_400.0),
        WEEK("wk", 604_800.0),
        /** Average month, defined as 1/12th of a Gregorian year. */
        MONTH("mo", 2_626_800.0),
        /** Average Gregorian year (365.2425 days). */
        YEAR("yr", 31_557_600.0);

        private final String symbol;
        private final double factor;

        Unit(String symbol, double factor) {
            validateFactor(factor);
            validateSymbol(symbol);
            this.symbol = symbol;
            this.factor = factor;
        }

        /**
         * Gets the enum constant from its string name (case-insensitive).
         * 
         * @param name_ The name of the enum constant.
         * @return The corresponding ETimes constant.
         */
        public static Timer.Unit fromName(String name_) {
            return Timer.Unit.valueOf(name_.toUpperCase(Locale.ENGLISH));
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
            return IUnit.toSentenceCase(this.name()) + "s";
        }
    }
    
    public Timer(double value, Timer.Unit unit) {
        super(value, unit);
    }

    @Override
    public Timer create(double val, Timer.Unit u) {
        return new Timer(val, u);
    }

    /** @return value expressed in seconds. */
    public double inSecond() {
        return this.inUnit(Timer.Unit.SECOND);
    }

    /** @return value expressed in minutes. */
    public double inMinute() {
        return this.inUnit(Timer.Unit.MINUTE);
    }

    /** @return value expressed in hours. */
    public double inHour() {
        return this.inUnit(Timer.Unit.HOUR);
    }

    /** @return value expressed in days. */
    public double inDay() {
        return this.inUnit(Timer.Unit.DAY);
    }

    /** @return value expressed in weeks. */
    public double inWeek() {
        return this.inUnit(Timer.Unit.WEEK);
    }

    /** @return value expressed in months (average). */
    public double inMonth() {
        return this.inUnit(Timer.Unit.MONTH);
    }

    /** @return value expressed in years. */
    public double inYear() {
        return this.inUnit(Timer.Unit.YEAR);
    }

    public static Timer fromDuration(double val, Timer.Unit unit) {
        return new Timer(val, unit).wrapPositive();
    }

    public static Timer fromTimestamp(double val, Timer.Unit unit) {
        return new Timer(val, unit).wrapPositive();
    }

    public static Timer fromHour(double hour) {
        return new Timer(hour, Timer.Unit.HOUR).wrapPositive();
    }

    public static Timer fromSecond(double second) {
        return new Timer(second, Timer.Unit.SECOND).wrapPositive();
    }
}
