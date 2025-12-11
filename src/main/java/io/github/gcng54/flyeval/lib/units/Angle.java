package io.github.gcng54.flyeval.lib.units;

import java.util.Locale;

/** Quantity representing angular measurements. */
public class Angle extends AQuantity<Angle, Angle.Unit> {

    /**
     * Defines standard units of angle. The base unit is {@link #RADIAN}.
     */
    public enum Unit implements IUnit<Unit> {
        RADIAN,        DEGREE,        DMS_DEGREE,        GRADIAN,        ARC_DEGREE,        ARC_MINUTE,        ARC_SECOND;
    
        @Override
        public String getSymbol() {
            String symbol = switch (this) {
                case RADIAN -> "rad";
                case DEGREE -> "°";
                case DMS_DEGREE -> "dms";
                case GRADIAN -> "gon";
                case ARC_DEGREE -> "arc°";
                case ARC_MINUTE -> "arc'";
                case ARC_SECOND -> "arc\"";
            };
            validateSymbol(symbol);
            return symbol;
        }

        @Override
        public double getFactor() {
            double factor = switch (this) {
                case RADIAN -> 1.0;
                case DEGREE -> Math.PI / 180.0;
                case DMS_DEGREE -> Math.PI / 180.0;
                case GRADIAN -> Math.PI / 200.0;
                case ARC_DEGREE -> Math.PI / 180.0;
                case ARC_MINUTE -> Math.PI / 10800.0;
                case ARC_SECOND -> Math.PI / 648000.0;
            };
            validateFactor(factor);
            return factor;
        }
        
        /**
         * Gets the enum constant from its string name (case-insensitive).
         *
         * @param name_ The name of the enum constant.
         * @return The corresponding EAngles constant.
         */
        public static Angle.Unit fromName(String name_) {
            return Angle.Unit.valueOf(name_.toUpperCase(Locale.ENGLISH));
        }

        @Override
        public String toString() {
            return IUnit.toSentenceCase(this.name()) + "s";
        }
    }

    /**
     * Creates an angle with the provided unit.
     *
     * @param value numeric value
     * @param unit  angle unit
     */
    public Angle(double value, Angle.Unit unit) {
        super(value, unit);
    }

    @Override
    public Angle create(double val, Angle.Unit u) {
        return new Angle(val, u);
    }

    /** @return angle expressed in decimal degrees. */
    public double inDegrees() {
        return this.inUnit(Angle.Unit.DEGREE);
    }

    /** @return angle expressed in radians. */
    public double inRadians() {
        return this.inUnit(Angle.Unit.RADIAN);
    }

    /** @return angle expressed in gradians. */
    public double inGradians() {
        return this.inUnit(Angle.Unit.GRADIAN);
    }

    /** @return angle expressed in arc minutes. */
    public double inArcMinutes() {
        return this.inUnit(Angle.Unit.ARC_MINUTE);
    }

    /** @return angle expressed in arc seconds. */
    public double inArcSeconds() {
        return this.inUnit(Angle.Unit.ARC_SECOND);
    }

    public static Angle ofAngle(double val, Angle.Unit unit) {
        return new Angle(val, unit);
    }

    public static Angle ofDegree(double degrees) {
        return new Angle(degrees, Angle.Unit.DEGREE);
    }

    public static Angle ofRadian(double radians) {
        return new Angle(radians, Angle.Unit.RADIAN);
    }

    public static Angle ofHeadingDeg(double degrees) {
        return Angle.ofDegree(degrees).wrapDegrees(0.0,360.0, Utils.EWrapMode.CYCLE);
    }

    public static Angle ofAzimuth(double val, Angle.Unit unit) {
        return new Angle(val, unit).wrap(0.0, unit.toBase(360.0), Utils.EWrapMode.CYCLE);
    }

    public static Angle ofAzimuthDeg(double degrees) {
        return Angle.ofDegree(degrees).wrapDegrees(0.0,360.0, Utils.EWrapMode.CYCLE);
    }

    public static Angle ofLatitude(double val, Angle.Unit unit) {
        return new Angle(val, unit).wrap(unit.toBase(-90.0), unit.toBase(90.0), Utils.EWrapMode.BOUNCE);
    }

    public static Angle ofLatitudeRad(double radians) {
        return Angle.ofRadian(radians).wrapDegrees(-90.0,90.0, Utils.EWrapMode.BOUNCE);
    }

    public static Angle ofLatitudeDeg(double degrees) {
        return Angle.ofDegree(degrees).wrapDegrees(-90.0,90.0, Utils.EWrapMode.BOUNCE);
    }

    public static Angle ofLongitude(double val, Angle.Unit unit) {
        return new Angle(val, unit).wrap(unit.toBase(-180.0), unit.toBase(180.0), Utils.EWrapMode.CYCLE);
    }

    public static Angle ofLongitudeDeg(double degrees) {
        return Angle.ofDegree(degrees).wrapDegrees(-180.0, 180.0, Utils.EWrapMode.CYCLE);
    }

    public static Angle ofLongitudeRad(double radians) {
        return Angle.ofRadian(radians).wrapDegrees(-180, 180.0, Utils.EWrapMode.CYCLE);
    }

    public static Angle ofElevation(double val, Angle.Unit unit) {
        return new Angle(val, unit).wrap(unit.toBase(-90.0), unit.toBase(90.0), Utils.EWrapMode.BOUNCE);
    }

    
    public static Angle ofElevationDeg(double degrees) {
        return Angle.ofDegree(degrees).wrapDegrees(-90.0,90.0, Utils.EWrapMode.BOUNCE);
    }

    public static Angle ofElevationRad(double radians) {
        return Angle.ofRadian(radians).wrapDegrees(-90.0,90.0, Utils.EWrapMode.BOUNCE);
    }

    /**
     * Converts this angle to the nearest compass orientation.
     *
     * @return orientation mapped from degrees
     */
    public EOrientation toOrientation() {
        return EOrientation.fromDegree(this.inDegrees());
    }

    /** @return DMS representation of this angle. */
    public DegMinSec toDMS() {
        return new DegMinSec(this.inDegrees());
    }

    /** @return DMS string representation. */
    public String toDMSString() {
        return this.toDMS().toString();
    }

    /** Computes the sine of this angle. */
    public double sin() {
        return Math.sin(this.inRadians());
    }

    /** Computes the cosine of this angle. */
    public double cos() {
        return Math.cos(this.inRadians());
    }

    /** Computes the tangent of this angle. */
    public double tan() {
        return Math.tan(this.inRadians());
    }

    /**
     * Delegates to {@link Utils#getAzimuthInRad(double, double)} for convenience.
     */
    public static Angle calcAzimuth(double x, double y) {
        return Angle.ofRadian(calcAzimuthRad(x, y));
    }

    /**
     * Calculates the azimuth in radians from the given x and y components.
     * The azimuth is measured clockwise from the north (y-axis).
     * <p>
     * The method uses Math.atan2 to compute the angle, which correctly handles
     * the quadrants and edge cases. The result is normalized to the range (0, 2π).
     *
     * @return The azimuth in radians, in the range [0, 2π).
     *         <h4>Examples:</h4>
     *
     *         <pre>{@code
     * double azimuth1 = calcAzimuthRad(0.0, 1.0); // 0.0 (north)
     * double azimuth2 = calcAzimuthRad(1.0, 0.0); // π/2 (east)
     * double azimuth3 = calcAzimuthRad(0.0, -1.0); // π (south)
     * double azimuth4 = calcAzimuthRad(-1.0, 0.0); // 3π/2 (west)
     * }</pre>
     */
    public static double calcAzimuthRad(double x, double y) {
    // If both components are zero, return 0 by convention (undefined direction)
    if (x == 0.0 && y == 0.0) {
        return 0.0;
    }
    // atan2 returns 0 for (0, 1) (north), which is correct for azimuth
    double radian = Math.atan2(x, y); // returns [-PI, PI], 0 = north, PI/2 = east
    if (radian < 0.0)
        radian += 2.0 * Math.PI; // normalize to [0, 2PI)
    // If an angle is extremely close to 0 or 2*PI, snap to 0.0 to avoid -0.0 or
    // floating-point noise
    if (Math.abs(radian) < 1E-9 || Math.abs(radian - 2.0 * Math.PI) < 1E-9)
        radian = 0.0;
    return radian;
}

    @Override
    public String toString() {
        if (Angle.Unit.DMS_DEGREE.equals(this.unit)) {
            return new DegMinSec(this.inDegrees()).toString();
        }
        return super.toString();
    }
}
