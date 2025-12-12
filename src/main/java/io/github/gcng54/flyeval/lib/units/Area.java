package io.github.gcng54.flyeval.lib.units;

import java.util.Locale;

/** Quantity representing area. */
public class Area extends AQuantity<Area, Area.Unit> {

    /**
     * Defines derived units of area. Each unit is the square of a corresponding
     * {@link ELengths} unit.
     * The base unit is square meters (m²).
     */
    public enum Unit implements IUnit<Unit> {
        SQ_METER(Length.Unit.METER),
        SQ_KILOMETER(Length.Unit.KILOMETER),
        SQ_CENTIMETER(Length.Unit.CENTIMETER),
        SQ_FOOT(Length.Unit.FOOT),
        SQ_INCH(Length.Unit.INCH),
        SQ_YARD(Length.Unit.YARD),
        SQ_MILE(Length.Unit.MILE),
        SQ_DATAMILE(Length.Unit.DATAMILE),
        SQ_NAUTICAL(Length.Unit.NAUTICAL);
    
        private final Length.Unit lengthunit;

        Unit(Length.Unit lengthunit) {
            this.lengthunit = lengthunit;
        }

        /**
         * Gets the enum constant from its string name (case-insensitive).
         * 
         * @param name_ The name of the enum constant.
         * @return The corresponding EAreas constant.
         */
        public static Area.Unit fromName(String name_) {
            return Area.Unit.valueOf(name_.toUpperCase(Locale.ENGLISH));
        }

        @Override
        public String getSymbol() {
            return lengthunit.getSymbol() + "²";
        }

        @Override
        public double getFactor() {
            return Math.pow(lengthunit.getFactor(), 2);
        }

        /**
         * Gets the underlying length unit used to derive this volume unit.
         *
         * @return The {@link Length.Unit} component of the area.
         */
        public Length.Unit getLengthUnit() {
            return lengthunit;
        }

        @Override
        public String toString() {
            return IUnit.toSentenceCase(this.name()) + "s";
        }
    }

    public Area(double value, Area.Unit unit) {
        super(value, unit);
    }

    public Area(double value, Length.Unit lengthUnit) {
        super(value, lengthUnit.getAreaUnit());
    }

    @Override
    public Area create(double val, Area.Unit u) {
        return new Area(val, u);
    }

    /** @return value expressed in meters. */
    public double inSqMeter() {
        return this.inUnit(Area.Unit.SQ_METER);
    }

    /** @return value expressed in kilometers. */
    public double inSqKilometer() {
        return this.inUnit(Area.Unit.SQ_KILOMETER);
    }

    public static Area ofSurface(double val, Area.Unit unit) {
        return new Area(val, unit).wrapPositive();
    }

    public static Area ofSurface(double val, Length.Unit unit) {
        return new Area(val, unit).wrapPositive();
    }

    public static Area ofSqKilometer(double sq_kilometer) {
        return new Area(sq_kilometer, Area.Unit.SQ_KILOMETER).wrapPositive();
    }

    public static Area ofSqMeter(double sq_meter) {
        return new Area(sq_meter, Area.Unit.SQ_METER).wrapPositive();
    }

    /** Multiplies area by length to produce volume. */
    public Volume multiply(Length l) {
        double squareMeters = this.getBase();
        double meters = l.getBase();
        return new Volume(squareMeters * meters, Volume.Unit.CU_METER);
    }

    /** Divides area by length to produce length. */
    public Length divide(Length length) {
        double squareMeters = this.getBase();
        double meters = length.getBase();
        return new Length(squareMeters / meters, Length.Unit.METER);
    }
}
