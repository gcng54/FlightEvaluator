package io.github.gcng54.flyeval.lib.units;

import java.util.Locale;

/** Quantity representing speed. */
public class Speed extends AQuantity<Speed, Speed.Unit> {

/**
     * Defines derived units of area. Each unit is the square of a corresponding
     * {@link ELengths} unit.
     * The base unit is square meters (m²).
     */
    public enum Unit implements IUnit<Unit> {
        METER_HR(Length.Unit.METER),
        KILOMETER_HR(Length.Unit.KILOMETER),
        CENTIMETER_HR(Length.Unit.CENTIMETER),
        FOOT_HR(Length.Unit.FOOT),
        INCH_HR(Length.Unit.INCH),
        YARD_HR(Length.Unit.YARD),
        MILE_HR(Length.Unit.MILE),
        DATAMILE_HR(Length.Unit.DATAMILE),
        NAUTICAL_HR(Length.Unit.NAUTICAL);
    
        private final Length.Unit lengthunit;

        Unit(Length.Unit lengthunit) {
            this.lengthunit = lengthunit;
        }

        /**
         * Gets the enum constant from its string name (case-insensitive).
         *
         * @param name_ The name of the enum constant.
         * @return The corresponding Volume constant.
         */
        public static Speed.Unit fromName(String name_) {
            return Speed.Unit.valueOf(name_.toUpperCase(Locale.ENGLISH));
        }

        @Override
        public String getSymbol() {
            return lengthunit.getSymbol() + "/h";
        }

        @Override
        public double getFactor() {
            return lengthunit.getFactor() / 3600.0;
        }

        /**
         * Gets the underlying length unit used to derive this volume unit.
         *
         * @return The {@link Length.Unit} component of the volume.
         */
        public Length.Unit getLengthUnit() {
            return lengthunit;
        }

        @Override
        public String toString() {
            return IUnit.toSentenceCase(this.name()) + "s";
        }
    }

    public Speed(double value, Speed.Unit unit) {
        super(value, unit);
    }

    /**
     * Calculates the speed of sound (Mach 1) for a given altitude using the ISA model.
     */
    public static Speed getSpeedOfSound(Length altitude) {
        double speedOfSound = calcSpeedOfSoundInMPS(altitude.inMeter());
        return new Speed(speedOfSound / 3.600, Speed.Unit.KILOMETER_HR);
    }

    @Override
    public Speed create(double val, Speed.Unit u) {
        return new Speed(val, u);
    }

    /** @return speed value expressed per second. */
    public double getValueSec() {
        return this.getValue() / 3600.0;
    }

    /** @return base speed expressed per second. */
    public double getBaseSec() {
        return this.getBase() / 3600.0;
    }

    /** @return speed in meters per second. */
    public double inMeterPerSec() {
        return this.inUnit(Speed.Unit.METER_HR) / 3600.0;
    }

    /** @return speed in kilometers per hour. */
    public double inMPS() {
        return this.inUnit(Speed.Unit.METER_HR) / 3600.0;
    }

    /** @return speed in knots. */
    public double inKnot() {
        return this.inUnit(Speed.Unit.NAUTICAL_HR);
    }


    /** @return speed in kilometers per hour. */
    public double inKPH() {
        return this.inUnit(Speed.Unit.KILOMETER_HR);
    }

    /** @return speed in meters per hour. */
    public double inMeterPerHr() {
        return this.inUnit(Speed.Unit.METER_HR);
    }

    public double inKilometerPerHr() {
        return this.inUnit(Speed.Unit.KILOMETER_HR);
    }

    public double inMilePerHr() {
        return this.inUnit(Speed.Unit.MILE_HR);
    }

    public double inNauticalPerHr() {
        return this.inUnit(Speed.Unit.NAUTICAL_HR);
    }

    /** Converts this speed value to its Mach number at a given altitude. */
    public double inMach(Length altitude) {
        return this.getBase() / getSpeedOfSound(altitude).getBase();
    }

    public static Speed ofMeterPerHr(double metersPerHour) {
        return new Speed(metersPerHour, Speed.Unit.METER_HR).wrapPositive();
    }

    public static Speed ofKilometerPerHr(double kilometersPerHour) {
        return new Speed(kilometersPerHour, Speed.Unit.KILOMETER_HR).wrapPositive();
    }

    public static Speed ofKnot(double knots) {
        return new Speed(knots, Speed.Unit.NAUTICAL_HR).wrapPositive();
    }

    public static Speed ofMach(double mach) {
        double speedOfSoundMPS = calcSpeedOfSoundInMPS(0.0);
        mach = mach * speedOfSoundMPS * 3.6;
        return new Speed(mach, Speed.Unit.KILOMETER_HR).wrapPositive();
    }

    public static Speed ofMach(double mach, Length altitude) {
        double speedOfSoundMPS = calcSpeedOfSoundInMPS(altitude.inMeter());
        mach = mach * speedOfSoundMPS * 3.6;
        return new Speed(mach, Speed.Unit.KILOMETER_HR).wrapPositive();
    }


    /** Multiples speed by time to produce a length. */
    public Length multiply(Timer timer) {
        double hour = timer.inUnit(Timer.Unit.HOUR);
        return new Length(this.getValue() * hour, this.unit.getLengthUnit());
    }

    /** Multiples speed by time to produce a length. */
    public Timer divide(Length length) {
        this.validateNotZero();
        double hour = this.inMeterPerHr() / length.inMeter();
        return new Timer(hour, Timer.Unit.HOUR);
    }

    /**
     * Calculates the speed of sound (Mach 1) for a given altitude using the
     * International Standard Atmosphere (ISA) model.
     *
     * @param altitude_meter The geometric altitude.
     * @return The speed of sound as a Speed quantity in m/s.
     */
    public static double calcSpeedOfSoundInMPS(double altitude_meter) {
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
}
