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
            String symbol = "L";
            switch (this) {
                case METER: symbol = "m";  break;
                case KILOMETER : symbol =  "km"; break;
                case CENTIMETER : symbol =  "cm"; break;
                case FOOT : symbol =  "ft"; break;
                case INCH : symbol =  "in"; break;
                case YARD : symbol =  "yd"; break;
                case MILE : symbol =  "mi"; break;
                case FLIGHTLEVEL : symbol =  "FL"; break; // 100 ft
                case NAUTICAL : symbol =  "NM"; break;
                case DATAMILE : symbol =  "DM"; break;
            };
            validateSymbol(symbol);
            return symbol;
        }

        @Override
        public double getFactor() {
            double factor = 1.0;
            switch (this) {
                case METER: factor = 1.0; break;
                case KILOMETER : factor = 1000.0; break;
                case CENTIMETER : factor = 0.01; break;
                case FOOT : factor = 0.3048; break;
                case INCH : factor = 0.0254; break;
                case YARD : factor = 0.9144; break;
                case MILE : factor = 1609.344; break;
                case FLIGHTLEVEL : factor = 30.48;  break; // 100 ft
                case NAUTICAL : factor = 1852.0; break;
                case DATAMILE : factor = 1828.8; break;

            };
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
         * @return The associated {@link EAreas} unit.
         */
        public Area.Unit getAreaUnit() {
            return Area.Unit.fromName("SQ_" + this.name());
        }

        /**
         * Gets the corresponding volume unit (e.g., METER -> CU_METER).
         *
         * @return The associated {@link EVolumes} unit.
         */
        public Volume.Unit getVolumeUnit() {
            return Volume.Unit.fromName("CU_" + this.name());
        }

        /**
         * Gets the corresponding speed unit (e.g., METER -> METER_HR).
         *
         * @return The associated {@link ESpeeds} unit.
         */
        public Speed.Unit getSpeedUnit() {
            return Speed.Unit.fromName(this.name() + "_HR");
        }
    }

    /**
     * Public static factory for Length.
     */
    public static Length of(double value, Length.Unit unit) {
        return new Length(value, unit);
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

    public static Length fromLength(double val, Length.Unit unit) {
        return new Length(val, unit).wrapPositive();
    }

    public static Length fromMeter(double meter) {
        return new Length(meter, Length.Unit.METER);
    }

    public static Length fromKilometer(double kilometer) {
        return new Length(kilometer, Length.Unit.KILOMETER);
    }

    public static Length fromFlightLevel(double fl_100ft) {
        return new Length(fl_100ft, Length.Unit.FLIGHTLEVEL);
    }

    public static Length fromDistance(double value, Length.Unit unit) {
        return new Length(value, unit).wrapPositive();
    }

    public static Length fromRangeNM(double nautical) {
        return new Length(nautical, Length.Unit.NAUTICAL).wrapPositive();
    }

    public static Length fromRangeKm(double kilometer) {
        return new Length(kilometer, Length.Unit.KILOMETER).wrapPositive();
    }

    public static Length fromDistanceKm(double kilometer) {
        return new Length(kilometer, Length.Unit.KILOMETER).wrapPositive();
    }

    public static Length fromDistanceMeter(double meter) {
        return new Length(meter, Length.Unit.METER).wrapPositive();
    }

    public static Length fromAltitude(double meter, Length.Unit unit) {
        return new Length(meter, unit).wrapPositive();
    }
    
    public static Length fromAltitudeMeter(double meter) {
        return new Length(meter, Length.Unit.METER).wrapPositive();
    }

    /**
     * Derivation: Length / Time = Speed.
     *
     * @param time time interval
     * @return speed using this length unit family
     */
    public Speed divide(Time time) {
        double hour = time.inUnit(Time.Unit.HOUR);
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
