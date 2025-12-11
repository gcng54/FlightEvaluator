package io.github.gcng54.flyeval.lib.units;

import java.util.Locale;

/**
 * Abstract base class representing a physical quantity with a value and unit.
 * <p>
 * This class provides a framework for quantities that have an associated unit
 * and value,
 * supporting arithmetic operations, unit conversions, and logical comparisons.
 * Concrete subclasses must implement the {@code create} factory method to
 * instantiate
 * specific quantity types.
 *
 * @param <Q> The concrete quantity type extending {@code AQuantity}.
 * @param <U> The unit type, extending {@code PhysicalUnits.IUnit}.
 */
public abstract class AQuantity<Q extends AQuantity<Q, U>, U extends IUnit<U>> {

    /** The numeric value expressed in the current unit. */
    protected final double value;
    /** The value converted into the unit's base representation. */
    protected final double baseValue;
    /** The unit associated with this quantity instance. */
    protected final U unit;

    /**
     * Constructs a quantity with the given value and unit.
     *
     * @param value numeric value in the provided unit
     * @param unit  unit describing the value
     */
    protected AQuantity(double value, U unit) {
        this.unit = unit;
        this.value = value;
        this.baseValue = this.toBase(value);
    }

    /**
     * Factory method to create a concrete quantity instance.
     *
     * @param val  value expressed in the provided unit
     * @param unit target unit for the new quantity
     * @return new concrete quantity
     */
    public abstract Q create(double val, U unit);

    public Q of(double val, U unit){
        return create(val, unit);
    }

    /**
     * Creates a new instance of {@code Q} with the specified value and the default
     * unit.
     *
     * @param val the numeric value to assign to the new quantity
     * @return a new instance of {@code Q} representing the specified value in the
     *         default unit
     */
    public Q create(double val) {
        return create(val, unit);
    }

    /**
     * Creates a new instance of {@code Q} representing the current base value in a
     * different unit.
     *
     * @param targetUnit target unit
     * @return new quantity converted to the requested unit
     */
    public Q create(U targetUnit) {
        return create(targetUnit.fromBase(getBase()), targetUnit);
    }

    /**
     * Creates a new quantity from a supplied base value in the same unit.
     *
     * @param baseValue base value to use
     * @return new quantity using the provided base value
     */
    public Q createBase(double baseValue) {
        return create(this.fromBase(baseValue), this.unit);
    }

    /**
     * Returns a new quantity with its base value wrapped within the provided bounds
     * using the selected wrap mode.
     *
     * @param minBase  lower bound in base units
     * @param maxBase  upper bound in base units
     * @param wrapMode wrapping strategy
     * @return wrapped quantity
     */
    public Q wrap(double minBase, double maxBase, Utils.EWrapMode wrapMode) {
        double val = getBase();
        if (wrapMode != Utils.EWrapMode.NONE) {
            val = Utils.wrapWithMode(getBase(), minBase, maxBase, wrapMode);
        }
        return create(fromBase(val), unit);
    }

    /**
     * Returns this quantity without applying any wrapping.
     *
     * @return unmodified quantity
     */
    public Q wrap() {
        return wrap(-Double.MAX_VALUE, Double.MAX_VALUE, Utils.EWrapMode.NONE);
    }

    /**
     * Returns a quantity wrapped to be non-negative.
     *
     * @return quantity whose base value is clamped to zero or greater
     */
    public Q wrapPositive() {
        return wrap(0.0, Double.POSITIVE_INFINITY, Utils.EWrapMode.BOUND);
    }

    /**
     * Gets the raw value in the current unit.
     *
     * @return value in current unit
     */
    public double getValue() {
        return value;
    }

    /**
     * Gets the value converted to the base unit.
     *
     * @return base value
     */
    public double getBase() {
        return baseValue;
    }

    /**
     * Gets the unit of this quantity.
     *
     * @return unit instance
     */
    public U getUnit() {
        return unit;
    }

    /**
     * Gets the scale factor of the unit.
     *
     * @return unit factor
     */
    public double getFactor() {
        return unit.getFactor();
    }

    /**
     * Gets the unit symbol.
     *
     * @return symbol string
     */
    public String getSymbol() {
        return unit.getSymbol();
    }

    /**
     * Converts a value in the current unit to its base representation.
     *
     * @param value value in current unit
     * @return base value
     */
    public double toBase(double value) {
        return this.unit.toBase(value);
    }

    /**
     * Converts a base value into the current unit.
     *
     * @param baseValue value expressed in base units
     * @return value in current unit
     */
    public double fromBase(double baseValue) {
        return this.unit.fromBase(baseValue);
    }

    /**
     * Checks whether the base value is finite and not NaN.
     *
     * @return true if the value is usable; false otherwise
     */
    public boolean isValid() {
        return !Double.isNaN(this.getBase()) && !Double.isInfinite(this.getBase());
    }

    /**
     * Checks whether the base value is finite and not NaN.
     *
     * @return true if the value is usable; false otherwise
     */
    public boolean isCloseZero() {
        return Math.abs(this.getBase()) <= 1e-10;
    }

    public void validateValue(){
        if (!this.isValid()) {
            throw new ArithmeticException("Quantity value is not valid");
        }
    }

    public void validateNotZero(){
        if (isCloseZero()) {
            throw new ArithmeticException("Cannot divide by a quantity with zero value");
        }
    }

    /**
     * Returns the quantity value expressed in a target unit.
     *
     * @param targetUnit unit to convert into
     * @return numeric value in the target unit
     */
    public double inUnit(U targetUnit) {
        return targetUnit.fromBase(getBase());
    }

    /**
     * Compares this quantity to another based on base values.
     *
     * @param other quantity to compare
     * @return negative if less, zero if equal, positive if greater
     */
    public int isCompareTo(AQuantity<?, U> other) {
        return Double.compare(this.getBase(), other.getBase());
    }

    public boolean less(AQuantity<?, U> other) {
        return this.isCompareTo(other) < 0;
    }

    public boolean lessOrEqual(AQuantity<?, U> other) {
        return this.isCompareTo(other) <= 0;
    }

    public boolean greater(AQuantity<?, U> other) {
        return this.isCompareTo(other) > 0;
    }

    public boolean greaterOrEqual(AQuantity<?, U> other) {
        return this.isCompareTo(other) >= 0;
    }

    public boolean equalsTo(AQuantity<?, U> other) {
        return this.isCompareTo(other) == 0;
    }

    /**
     * Adds another quantity (converted via base) and returns a new instance.
     */
    public Q add(AQuantity<?, U> other) {
        return createBase(this.getBase() + other.getBase());
    }

    /**
     * Subtracts another quantity and returns a new instance.
     */
    public Q subtract(AQuantity<?, U> other) {
        return createBase(this.getBase() - other.getBase());
    }

    /**
     * Multiplies this quantity by a scalar.
     */
    public Q multiply(double scalar) {
        return createBase(this.getBase() * scalar);
    }

    /**
     * Returns the absolute value of this quantity.
     */
    public Q absolute() {
        return create(Math.abs(this.getValue()));
    }

    /**
     * Negates this quantity.
     */
    public Q negate() {
        return create(-1.0 * this.getValue(), this.unit);
    }

    /**
     * Divides this quantity by a scalar.
     */
    public Q divide(double scalar) {
        if (scalar == 0)
            throw new ArithmeticException("Cannot divide by a quantity with zero value");
        return createBase(this.getBase() / scalar);
    }

    /**
     * Divides this quantity by another quantity of the same unit family.
     */
    public double divide(AQuantity<?, U> other) {
        other.validateNotZero();
        double otherBase = other.getBase();
        return this.getBase() / otherBase;
    }

    /**
     * Returns the multiplicative inverse of this quantity.
     */
    public Q invert() {
        this.validateNotZero();
        return createBase(1.0 / this.getBase());
    }

    /**
     * Floors the value of this quantity.
     */
    public Q floor() {
        return create(Math.floor(this.getValue()));
    }

    /**
     * Ceils the value of this quantity.
     */
    public Q ceil() {
        return create(Math.ceil(this.getValue()));
    }

    /**
     * Rounds this quantity to the given number of decimal places.
     */
    public Q round(int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        double roundedValue = Math.round(this.getValue() * scale) / scale;
        return create(roundedValue);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%.3f %s", getValue(), unit.getSymbol());
    }
}
