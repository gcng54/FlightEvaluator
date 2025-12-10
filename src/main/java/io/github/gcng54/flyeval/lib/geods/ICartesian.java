package io.github.gcng54.flyeval.lib.geods;

import org.jetbrains.annotations.NotNull;

import io.github.gcng54.flyeval.lib.units.*;

/**
 * Interface for Cartesian 3D vectors.
 *
 * @param <T> The concrete Cartesian type implementing this interface.
 */
public interface ICartesian<T extends ICartesian<T>> {

    // ABSTRACTS

    Length getX();

    Length getY();

    Length getZ();

    default double getBaseX() {
        return getX().getBase();
    }

    default double getBaseY() {
        return getY().getBase();
    }

    default double getBaseZ() {
        return getZ().getBase();
    }

    T create(Length x, Length y, Length z);

    // DEFAULTS

    default Spherical toSpherical() {
        double rangeVal = magnitude();
        if (rangeVal < 1e-10) {
            return Spherical.ZERO;
        }
        double x = getBaseX();
        double y = getBaseY();
        double z = getBaseZ();
        double az = Math.atan2(y, x);
        double el = Math.asin(z / rangeVal);

        return new Spherical(Angle.fromAngle(az, Angle.Unit.RADIAN),
                Angle.fromAngle(el, Angle.Unit.RADIAN),
                Length.of(rangeVal, Length.Unit.METER));
    }

    default T copy() {
        return create(getX(), getY(), getZ());
    }

    default double[] toArray() {
        return new double[] { getBaseX(), getBaseY(), getBaseZ() };
    }

    default T clamp(@NotNull ICartesian<?> min, @NotNull ICartesian<?> max, Utils.EWrapMode wrapMode) {
        return create(
                this.getX().wrap(min.getBaseX(), max.getBaseX(), wrapMode),
                this.getY().wrap(min.getBaseY(), max.getBaseY(), wrapMode),
                this.getZ().wrap(min.getBaseZ(), max.getBaseZ(), wrapMode));
    }

    default String toStringXYZ() {
        return String.format("X: %s, Y: %s, Z: %s", getX(), getY(), getZ());
    }

    default double hypotXYZ(@NotNull ICartesian<?> other) {
        double dx = this.getBaseX() - other.getBaseX();
        double dy = this.getBaseY() - other.getBaseY();
        double dz = this.getBaseZ() - other.getBaseZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    default double hypotXY(@NotNull ICartesian<?> other) { // This should probably return a Types.Length
        double dx = this.getBaseX() - other.getBaseX();
        double dy = this.getBaseY() - other.getBaseY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    default T add(@NotNull ICartesian<?> other) { // Assumes same units
        return create(this.getX().add(other.getX()),
                this.getY().add(other.getY()),
                this.getZ().add(other.getZ()));
    }

    default T subtract(@NotNull ICartesian<?> other) { // Assumes same units
        return create(this.getX().subtract(other.getX()),
                this.getY().subtract(other.getY()),
                this.getZ().subtract(other.getZ()));
    }

    default T rsubtract(@NotNull ICartesian<?> other) {
        return create(other.getX().subtract(this.getX()),
                other.getY().subtract(this.getY()),
                other.getZ().subtract(this.getZ()));
    }

    default T ratio(@NotNull ICartesian<?> other) {
        if (other.isAnyZero(1e-10)) {
            throw new IllegalArgumentException("Cannot div by zero.");
        }
        return create(this.getX().create(this.getX().divide(other.getX())),
                this.getY().create(this.getY().divide(other.getY())),
                this.getZ().create(this.getZ().divide(other.getZ())));
    }

    default T invert() {
        if (this.isAnyZero(1e-10)) {
            throw new IllegalArgumentException("Cannot inv position with zero component.");
        }
        return create(this.getX().invert(),
                this.getY().invert(),
                this.getZ().invert());
    }

    default T negate() {
        return create(this.getX().negate(), this.getY().negate(), this.getZ().negate());
    }

    default T scale(double factor) {
        return create(this.getX().multiply(factor),
                this.getY().multiply(factor),
                this.getZ().multiply(factor));
    }

    default T transform(@NotNull ICartesian<?> other, double scalar) {
        return create(
                this.getX().add(other.getX().multiply(scalar)),
                this.getY().add(other.getY().multiply(scalar)),
                this.getZ().add(other.getZ().multiply(scalar)));
    }

    default T divide(double divisor) {
        if (Math.abs(divisor) < 1e-10) {
            throw new IllegalArgumentException("Division by zero is not allowed.");
        }
        return create(this.getX().divide(divisor),
                this.getY().divide(divisor),
                this.getZ().divide(divisor));
    }

    default double dot(@NotNull ICartesian<?> other) {
        return this.getBaseX() * other.getBaseX()
                + this.getBaseY() * other.getBaseY()
                + this.getBaseZ() * other.getBaseZ();
    }

    default double magnitude() {
        double x = this.getBaseX();
        double y = this.getBaseY();
        double z = this.getBaseZ();
        return Math.sqrt(x * x + y * y + z * z);
    }

    default T normalize() {
        double mag = magnitude();
        if (mag < 1e-10) {
            throw new IllegalArgumentException("Cannot normalize a zero-Types.Length vector.");
        }
        // Normalizing results in a unitless vector, but our structure requires
        // Types.Length.
        // We create new Types.Lengths representing the unit vector components.
        return create(
                this.getX().createBase(getBaseX() / mag),
                this.getY().createBase(getBaseY() / mag),
                this.getZ().createBase(getBaseZ() / mag));
    }

    default T cross(@NotNull ICartesian<?> other) {
        double x1 = this.getBaseX(), y1 = this.getBaseY(), z1 = this.getBaseZ();
        double x2 = other.getBaseX(), y2 = other.getBaseY(), z2 = other.getBaseZ();
        return create(this.getX().createBase(y1 * z2 - z1 * y2),
                this.getY().createBase(z1 * x2 - x1 * z2),
                this.getZ().createBase(x1 * y2 - y1 * x2));
    }

    default double angle(@NotNull ICartesian<?> other) {
        double dotProd = this.dot(other);
        double mags = this.magnitude() * other.magnitude();
        if (mags < 1e-10) {
            throw new IllegalArgumentException("Cannot compute angle with zero-Types.Length vector.");
        }
        double cosTheta = Math.max(-1.0, Math.min(1.0, dotProd / mags));
        return Math.acos(cosTheta);
    }

    default boolean equals(@NotNull ICartesian<?> other, double epsilon) {
        return Math.abs(this.getBaseX() - other.getBaseX()) <= epsilon &&
                Math.abs(this.getBaseY() - other.getBaseY()) <= epsilon &&
                Math.abs(this.getBaseZ() - other.getBaseZ()) <= epsilon;
    }

    default boolean isZero(double epsilon) {
        return Math.abs(this.getBaseX()) <= epsilon &&
                Math.abs(this.getBaseY()) <= epsilon &&
                Math.abs(this.getBaseZ()) <= epsilon;
    }

    default boolean isAnyZero(double epsilon) {
        return Math.abs(this.getBaseX()) <= epsilon || Math.abs(this.getBaseY()) <= epsilon
                || Math.abs(this.getBaseZ()) <= epsilon;
    }

    default boolean isValid() {
        return getX().isValid() && getY().isValid() && getZ().isValid();
    }

    default boolean greater(@NotNull ICartesian<T> other) {
        return this.getX().greater(other.getX()) &&
                this.getY().greater(other.getY()) &&
                this.getZ().greater(other.getZ());
    }

    default boolean less(@NotNull ICartesian<T> other) {
        return this.getX().less(other.getX()) &&
                this.getY().less(other.getY()) &&
                this.getZ().less(other.getZ());
    }

    default boolean greaterOrEqual(@NotNull ICartesian<T> other) {
        return this.getX().greaterOrEqual(other.getX()) &&
                this.getY().greaterOrEqual(other.getY()) &&
                this.getZ().greaterOrEqual(other.getZ());
    }

    default boolean lessOrEqual(@NotNull ICartesian<T> other) {
        return this.getX().lessOrEqual(other.getX()) &&
                this.getY().lessOrEqual(other.getY()) &&
                this.getZ().lessOrEqual(other.getZ());
    }

    default Cartesian toCartesian() {
        return new Cartesian(this.getX(), this.getY(), this.getZ());
    }

}

