package io.github.gcng54.flyeval.lib.units;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

/** Record representing an angle in degrees, minutes, and seconds. */
public record DegMinSec(int Degree, int Minute, double Second) {

    /** Builds a DMS value from degree and decimal minutes. */
    public DegMinSec(int Degree, double minutes) {
        this(Degree, (int) minutes, (minutes - (int) minutes) * 60);
    }

    /**
     * Creates a DegMinSec record from a decimal degree value, handling
     * rounding and normalization so that minutes/seconds never roll to 60.
     */
    public DegMinSec(double degrees) {
        final double epsilon = 1e-9;
        boolean isNegative = degrees < 0;
        double absDegrees = Math.abs(degrees);

        int d = (int) absDegrees;
        double m_decimal = (absDegrees - d) * 60.0;
        int m = (int) m_decimal;
        double s = (m_decimal - m) * 60.0;

        if (s >= 60.0 - epsilon) {
            s = 0;
            m++;
        }
        if (m >= 60) {
            m = 0;
            d++;
        }

        this(isNegative ? -d : d, m, s);
    }

    public DegMinSec() {
        this(0, 0, 0);
    }

    /**
     * Creates an {@link Angle} by parsing a coordinate string in ICAO DMS_S format.
     */
    public static double parseDMS(@NotNull String dmsString) {
        if (dmsString.isBlank()) {
            throw new IllegalArgumentException("Input DMS string cannot be null or empty.");
        }

        String normalized = dmsString.trim().toUpperCase(Locale.ENGLISH);

        boolean isNegative = normalized.endsWith("W") || normalized.endsWith("S");
        if (!isNegative && !normalized.endsWith("E") && !normalized.endsWith("N")) {
            throw new IllegalArgumentException("DMS string must end with a direction (N, S, E, W).");
        }

        java.util.regex.Pattern pattern = java.util.regex.Pattern
                .compile("(\\d+)[°\\sDEG]*(\\d+)['\\sMIN]*([0-9.]+)\"?");
        java.util.regex.Matcher matcher = pattern.matcher(normalized);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid DMS format: " + dmsString);
        }

        double degrees = Double.parseDouble(matcher.group(1));
        double minutes = Double.parseDouble(matcher.group(2));
        double seconds = Double.parseDouble(matcher.group(3));

        minutes += Math.floor(seconds / 60.0);
        seconds %= 60.0;
        degrees += Math.floor(minutes / 60.0);
        minutes %= 60.0;

        double totalDegrees = degrees + (minutes / 60.0) + (seconds / 3600.0);

        if (isNegative && totalDegrees > 0) {
            totalDegrees = -totalDegrees;
        }

        return totalDegrees;
    }

    /** Converts this DMS value to decimal degrees. */
    public double inDegree() {
        return Degree + (Minute / 60.0) + (Second / 3600.0);
    }

    /** Converts this DMS value to total seconds of arc. */
    public double inSecond() {
        return Degree * 3600 + Minute * 60.0 + Second;
    }

    /** Returns the absolute value of the degrees component. */
    public int absDegree() {
        return Math.abs(Degree);
    }

    /** Returns the integer part of the seconds component. */
    public int intSecond() {
        return (int) Second();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        DegMinSec that = (DegMinSec) o;
        return Degree == that.Degree && Minute == that.Minute && Double.compare(Second, that.Second) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Degree, Minute, Second);
    }

    /** Parses a signed DMS string (e.g., "-123°45'06.7\"") without cardinal suffix. */
    public static double parseSignedDMS(@NotNull String dmsString) {
        if (dmsString.isBlank()) {
            throw new IllegalArgumentException("Input DMS string cannot be null or empty.");
        }

        String normalized = dmsString.trim().toUpperCase(Locale.ENGLISH);

        java.util.regex.Matcher numberMatcher = java.util.regex.Pattern
                .compile("([+-]?\\d+(?:\\.\\d+)?)")
                .matcher(normalized);

        double[] parts = new double[3];
        int count = 0;
        while (numberMatcher.find() && count < 3) {
            parts[count] = Double.parseDouble(numberMatcher.group(1));
            count++;
        }

        if (count < 3) {
            throw new IllegalArgumentException("Invalid DMS format: " + dmsString);
        }

        double degrees = parts[0];
        double minutes = parts[1];
        double seconds = parts[2];

        double sign = Math.signum(degrees);
        if (sign == 0)
            sign = 1.0;

        double absDeg = Math.abs(degrees);
        double absMin = Math.abs(minutes);
        double absSec = Math.abs(seconds);

        double totalDegrees = absDeg + (absMin / 60.0) + (absSec / 3600.0);
        return sign * totalDegrees;
    }

    @Override
    public @NotNull String toString() {
        return String.format(Locale.ENGLISH, "%d°%02d'%05.2f\"", Degree, Minute, Second);
    }
}
