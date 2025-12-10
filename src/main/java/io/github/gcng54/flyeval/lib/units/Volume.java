package io.github.gcng54.flyeval.lib.units;

import java.util.Locale;

/** Quantity representing volume. */
public class Volume extends AQuantity<Volume, Volume.Unit> {

    /**
     * Defines derived units of area. Each unit is the square of a corresponding
     * {@link ELengths} unit.
     * The base unit is square meters (m²).
     */
    public enum Unit implements IUnit<Unit> {
        CU_METER(Length.Unit.METER),
        CU_KILOMETER(Length.Unit.KILOMETER),
        CU_CENTIMETER(Length.Unit.CENTIMETER),
        CU_FOOT(Length.Unit.FOOT),
        CU_INCH(Length.Unit.INCH),
        CU_YARD(Length.Unit.YARD),
        CU_MILE(Length.Unit.MILE),
        CU_DATAMILE(Length.Unit.DATAMILE),
        CU_NAUTICAL(Length.Unit.NAUTICAL);
    
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
        public static Volume.Unit fromName(String name_) {
            return Volume.Unit.valueOf(name_.toUpperCase(Locale.ENGLISH));
        }

        @Override
        public String getSymbol() {
            return lengthunit.getSymbol() + "³";
        }

        @Override
        public double getFactor() {
            return Math.pow(lengthunit.getFactor(), 3);
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

    public Volume(double value, Volume.Unit unit) {
        super(value, unit);
    }

    public Volume(double value, Length.Unit lengthUnit) {
        super(value, lengthUnit.getVolumeUnit());
    }

    @Override
    public Volume create(double val, Volume.Unit u) {
        return new Volume(val, u);
    }

    /** @return value expressed in meters. */
    public double inCuMeter() {
        return this.inUnit(Volume.Unit.CU_METER);
    }

    /** @return value expressed in kilometers. */
    public double inCuKilometer() {
        return this.inUnit(Volume.Unit.CU_KILOMETER);
    }

    public static Volume fromSpace(double val, Volume.Unit unit) {
        return new Volume(val, unit).wrapPositive();
    }

    public static Volume fromSpace(double val, Length.Unit unit) {
        return new Volume(val, unit).wrapPositive();
    }
    
    public static Volume fromSpaceMeter(double cu_meter) {
        return new Volume(cu_meter, Volume.Unit.CU_METER).wrapPositive();
    }

    public static Volume fromCuMeter(double cu_meter) {
        return new Volume(cu_meter, Volume.Unit.CU_METER).wrapPositive();
    }

    /** Divides volume by length to produce area. */
    public Area divide(Length l) {
        double cubicMeters = this.getBase();
        double meters = l.getBase();
        return new Area(cubicMeters / meters, Area.Unit.SQ_METER);
    }

    /** Divides volume by area to produce length. */
    public Length divide(Area a) {
        double cubicMeters = this.getBase();
        double squareMeters = a.getBase();
        return new Length(cubicMeters / squareMeters, Length.Unit.METER);
    }

}
