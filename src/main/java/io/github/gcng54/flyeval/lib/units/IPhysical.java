package io.github.gcng54.flyeval.lib.units;

import java.util.Locale;

public interface IPhysical<Q> {

    Double getNumeric();

    String getSymbol();

    public Q from(Double numeric_);

    default String toStr(String symbol_) {
        if (symbol_ == null || symbol_.isEmpty()) {
            return String.format(Locale.ENGLISH, "%.3f", getNumeric());
        }
        return String.format(Locale.ENGLISH, "%.3f %s", getNumeric(), symbol_);
    }

    public static Double wrapPositive(Double numeric_) {
        if (numeric_ == null || numeric_.isNaN() || numeric_.isInfinite() || numeric_ < 0) {
            numeric_ = 0.0;
        }
        return numeric_;
    }

}
