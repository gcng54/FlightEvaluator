package io.github.gcng54.flyeval.lib.units;

import java.util.Locale;

/**
 * Represents a set of physical dimensions, each associated with:
 * <ul>
 * <li>a human-readable name, and</li>
 * <li>a base {@link IUnit} used as the canonical unit for that dimension.</li>
 * </ul>
 *
 * <p>
 * The available dimensions include common physical quantities such as
 * length, angle, time, speed, area, volume, pressure, and temperature.
 * Each dimension exposes its base unit through {@link #getBaseUnit()}, and
 * provides a readable label via {@link #toString()}.
 * </p>
 *
 * <p>
 * A dimension can be obtained from its name using
 * {@link #fromName(String)}, which looks up the enum constant by converting
 * the provided name to upper case using the English locale. This means the
 * lookup is case-insensitive with respect to standard English casing rules,
 * but the input must still match one of the declared enum constant names.
 * </p>
 */
enum EDimensions {
    LENGTH("Length", Length.Unit.METER),
        ANGLE("Angle", Angle.Unit.RADIAN),
        TIME("Time", Time.Unit.SECOND),
        SPEED("Speed", Speed.Unit.METER_HR),
        AREA("Area", Area.Unit.SQ_METER),
        VOLUME("Volume", Volume.Unit.CU_METER),
        PRESSURE("Pressure", Pressure.Unit.PASCAL),
        TEMPERATURE("Temperature", Temperature.Unit.KELVIN);

    private final String name;
    private final IUnit<?> baseUnit;

    EDimensions(String name, IUnit<?> baseUnit) {
        this.name = name;
        this.baseUnit = baseUnit;
    }

    public IUnit<?> getBaseUnit() {
        return baseUnit;
    }

    @Override
    public String toString() {
        return name;
    }

    public static EDimensions fromName(String name_) {
        return EDimensions.valueOf(name_.toUpperCase(Locale.ENGLISH));
    }
}
