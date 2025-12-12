package io.github.gcng54.flyeval.lib.units;

import java.util.Locale;

/** Quantity representing length or distance. */
public class Length extends AQuantity<Length, Length.Unit> {

    /**
     * Length units.
     */
    public enum Unit implements IUnit<Unit> {
        METER, KILOMETER, CENTIMETER, FOOT, INCH, YARD, MILE, FLIGHTLEVEL, NAUTICAL, DATAMILE;

        @Override
        public String getSymbol() {
            String symbol = switch (this) {
                case METER -> "m";
                case KILOMETER -> "km";
                case CENTIMETER -> "cm";
                case FOOT -> "ft";
                case INCH -> "in";
                case YARD -> "yd";
                case MILE -> "mi";
                case FLIGHTLEVEL -> "FL"; // 100 ft
                case NAUTICAL -> "NM";
                case DATAMILE -> "DM";
            };
            validateSymbol(symbol);
            return symbol;
        }

        @Override
        public double getFactor() {
            double factor = switch (this) {
                case METER -> 1.0;
                case KILOMETER -> 1000.0;
                case CENTIMETER -> 0.01;
                case FOOT -> 0.3048;
                case INCH -> 0.0254;
                case YARD -> 0.9144;
                case MILE -> 1609.344;
                case FLIGHTLEVEL -> 30.48; // 100 ft
                case NAUTICAL -> 1852.0;
                case DATAMILE -> 1828.8; // 6000 ft
            };
            ;
            validateFactor(factor);
            return factor;
        }

        /**
         * Gets the enum constant from its string name (case-insensitive).
         *
         * @param name_ The name of the enum constant.
         * @return The corresponding ELengths constant.
         */
        public static Length.Unit fromName(String name_) {
            return Length.Unit.valueOf(name_.toUpperCase(Locale.ENGLISH));
        }

        @Override
        public String toString() {
            return switch (this) {
                case FOOT -> "Feet";
                case INCH -> "Inches";
                case FLIGHTLEVEL -> "FlightLevel";
                default -> IUnit.toSentenceCase(this.name()) + "s";
            };
        }

        /**
         * Gets the corresponding area unit (e.g., METER -> SQ_METER).
         *
         * @return The associated {@link Area.Unit} unit.
         */
        public Area.Unit getAreaUnit() {
            return Area.Unit.fromName("SQ_" + this.name());
        }

        /**
         * Gets the corresponding volume unit (e.g., METER -> CU_METER).
         *
         * @return The associated {@link Volume.Unit} unit.
         */
        public Volume.Unit getVolumeUnit() {
            return Volume.Unit.fromName("CU_" + this.name());
        }

        /**
         * Gets the corresponding speed unit (e.g., METER -> METER_HR).
         *
         * @return The associated {@link Speed.Unit} unit.
         */
        public Speed.Unit getSpeedUnit() {
            return Speed.Unit.fromName(this.name() + "_HR");
        }
    }

    /**
     * Creates a length value with the provided unit.
     *
     * @param value numeric value
     * @param unit  length unit
     */
    public Length(double value, Length.Unit unit) {
        super(value, unit);
    }

    @Override
    public Length create(double val, Length.Unit u) {
        return new Length(val, u);
    }


    /** @return value expressed in meters. */
    public double inMeter() {
        return this.inUnit(Length.Unit.METER);
    }

    /** @return value expressed in kilometers. */
    public double inKilometer() {
        return this.inUnit(Length.Unit.KILOMETER);
    }

    /** @return value expressed in statute miles. */
    public double inMile() {
        return this.inUnit(Length.Unit.MILE);
    }

    /** @return value expressed in feet. */
    public double inFoot() {
        return this.inUnit(Length.Unit.FOOT);
    }

    /** @return value expressed in inches. */
    public double inInch() {
        return this.inUnit(Length.Unit.INCH);
    }

    /** @return value expressed in yards. */
    public double inYard() {
        return this.inUnit(Length.Unit.YARD);
    }

    /** @return value expressed in centimeters. */
    public double inCentimeter() {
        return this.inUnit(Length.Unit.CENTIMETER);
    }

    /** @return value expressed in nautical miles. */
    public double inNautical() {
        return this.inUnit(Length.Unit.NAUTICAL);
    }

    /** @return value expressed in datamiles. */
    public double inDatamile() {
        return this.inUnit(Length.Unit.DATAMILE);
    }

    // STATICS

    public static Length ofLength(double val, Length.Unit unit) {
        return new Length(val, unit).wrapPositive();
    }

    public static Length ofMeter(double meter) {
        return new Length(meter, Length.Unit.METER);
    }

    public static Length ofKilometer(double kilometer) {
        return new Length(kilometer, Length.Unit.KILOMETER);
    }

    public static Length ofFlightLevel(double fl_100ft) {
        return new Length(fl_100ft, Length.Unit.FLIGHTLEVEL);
    }

    public static Length ofDistance(double value, Length.Unit unit) {
        return new Length(value, unit).wrapPositive();
    }
    public static Length ofDistanceKm(double kilometer) {
        return ofKilometer(kilometer).wrapPositive();
    }
    public static Length ofDistanceMt(double meter) {
        return ofMeter(meter).wrapPositive();
    }

    public static Length ofAltitude(double meter, Length.Unit unit) {
        return new Length(meter, unit).wrapPositive();
    }
    
    public static Length ofAltitudeMt(double meter) {
        return ofMeter(meter).wrapPositive();
    }

    public static Length ofRangeNM(double nautical) {
        return new Length(nautical, Length.Unit.NAUTICAL).wrapPositive();
    }

    public static Length ofRangeKm(double kilometer) {
        return ofKilometer(kilometer).wrapPositive();
    }

    /**
     * Derivation: Length / Time = Speed.
     *
     * @param timer time interval
     * @return speed using this length unit family
     */
    public Speed divide(Timer timer) {
        double hour = timer.inUnit(Timer.Unit.HOUR);
        return new Speed(this.getValue() / hour, this.unit.getSpeedUnit());
    }

    /**
     * Derivation: Length * Length = Area.
     *
     * @param other multiplicand length
     * @return area expressed using this length's area family
     */
    public Area multiply(Length other) {
        double baseArea = this.getBase() * other.getBase();
        return new Area(this.fromBase(baseArea), this.unit.getAreaUnit());
    }
}
