package io.github.gcng54.flyeval.lib.geods;

import io.github.gcng54.flyeval.lib.units.*;

/**
 * Interface for Spherical 3D vectors.
 *
 * @param <T> The concrete Spherical type implementing this interface.
 */
public interface ISpherical<T extends ISpherical<T>> {

    Angle getAzimuth();

    Angle getElevation();

    Length getRange();

    T create(Angle azimuth, Angle elevation, Length range);

    /**
     * Converts spherical coordinates to Cartesian coordinates using a specified
     * range.
     *
     * @param range The range (radius) to use for the conversion.
     * @return A new ICartesian object.
     */
    default ICartesian<?> toCartesian(Length range) {
        double az = getAzimuth().inRadians();
        double el = getElevation().inRadians();
        double r = range.getBase();
        Length x = this.getRange().createBase(r * Math.cos(el) * Math.cos(az));
        Length y = this.getRange().createBase(r * Math.cos(el) * Math.sin(az));
        Length z = this.getRange().createBase(r * Math.sin(el));

        return new Cartesian(x, y, z);
    }

    /**
     * Converts spherical coordinates to Cartesian coordinates using this instance's
     * own range.
     *
     * @return A new ICartesian object.
     */
    default ICartesian<?> toCartesian() {
        return toCartesian(getRange());
    }
}
