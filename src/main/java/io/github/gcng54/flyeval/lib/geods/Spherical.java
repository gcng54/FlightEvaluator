package io.github.gcng54.flyeval.lib.geods;

import io.github.gcng54.flyeval.lib.units.*;

/**
 * Record representing a 3D Spherical vector with azimuth, elevation, and range
 * components as {@link Angle} and {@link Length} quantities.
 *
 * @param azimuth   The azimuth angle.
 * @param elevation The elevation angle.
 * @param range     The range (distance).
 */
public record Spherical(Angle azimuth, Angle elevation, Length range)
        implements ISpherical<Spherical> {

    public static final Spherical ZERO = new Spherical(
            Angle.ofAzimuthDeg(0.0),
            Angle.ofElevationDeg(0.0),
            Length.ofRangeKm(0.0));

    public Spherical {
        if (azimuth == null || elevation == null || range == null) {
            throw new IllegalArgumentException("Spherical components cannot be null.");
        }
    }

    @Override
    public Spherical create(Angle azimuth, Angle elevation, Length range) {
        return new Spherical(azimuth, elevation, range);
    }

    @Override
    public String toString() {
        return String.format("Az: %s, El: %s, R: %s",
                azimuth.create(Angle.Unit.DEGREE),
                elevation.create(Angle.Unit.DEGREE),
                range.create(Length.Unit.METER));
    }

    @Override
    public Angle getAzimuth() {
        return this.azimuth;
    }

    @Override
    public Angle getElevation() {
        return this.elevation;
    }

    @Override
    public Length getRange() {
        return this.range;
    }
}
