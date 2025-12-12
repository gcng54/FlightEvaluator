package io.github.gcng54.flyeval.gui;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.gcng54.flyeval.lib.units.*;
import io.github.gcng54.flyeval.lib.geods.*;


/**
 * A utility class providing static methods for parsing values from JavaFX form controls.
 * This class centralizes parsing logic for various data types used across different controllers,
 * ensuring consistency and reducing code duplication. It includes methods for parsing primitive
 * types like doubles and complex types like Angles, Lengths, and Geodetic coordinates,
 * along with integrated validation and error handling via a custom {@link ValidationException}.
 */
public final class FormParserUtils {

    private FormParserUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Custom exception for input validation failures within the forms.
     */
    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    /**
     * Parses a {@link Double} from a {@link TextField}.
     *
     * @param field The text field to parse.
     * @return The parsed double, or null if the field is blank.
     * @throws ValidationException if the text is not a valid double.
     */
    @Nullable
    public static Double parseDouble(TextField field) throws ValidationException {
        return parseDouble(field, field.getPromptText(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
    }

    /**
     * Parses a {@link Double} from a {@link TextField} with validation.
     *
     * @param field The text field to parse.
     * @param fieldName The name of the field for error messages.
     * @param min The minimum allowed value.
     * @param max The maximum allowed value.
     * @param isOptional If true, blank input returns null; otherwise, it throws an exception.
     * @return The parsed double, or null if optional and blank.
     * @throws ValidationException if validation fails.
     */
    @Nullable
    public static Double parseDouble(TextField field, String fieldName, double min, double max, boolean isOptional) throws ValidationException {
        String text = field.getText();
        if (text == null || text.isBlank()) {
            if (isOptional) return null;
            throw new ValidationException(fieldName + " cannot be empty.");
        }
        try {
            String normalized = text.trim().replace(',', '.');
            double value = Double.parseDouble(normalized);
            if (value < min || value > max) {
                throw new ValidationException(String.format("%s must be between %.2f and %.2f.", fieldName, min, max));
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new ValidationException("Invalid number format for " + fieldName + ".");
        }
    }

    /**
    * Parses an {@link Angle} from a {@link TextField} and {@link ComboBox}.
     *
     * @param field The text field containing the angle value.
     * @param unitCombo The combo box for the angle unit.
     * @param fieldName The name of the field for error messages.
     * @param minDeg The minimum allowed value in degrees.
     * @param maxDeg The maximum allowed value in degrees.
     * @param isOptional If true, blank input returns null.
     * @return The parsed Angle, or null if optional and blank.
     * @throws ValidationException if validation fails.
     */
    @Nullable
    public static Angle parseAngle(TextField field, ComboBox<Angle.Unit> unitCombo, String fieldName, double minDeg, double maxDeg, boolean isOptional) throws ValidationException {
        String text = field.getText();
        if (text == null || text.isBlank()) {
            if (isOptional) return null;
            throw new ValidationException(fieldName + " cannot be empty.");
        }
        Angle.Unit unit = unitCombo.getValue() == null ? Angle.Unit.DEGREE : unitCombo.getValue();
        try {
            String normalized = text.trim().replace(',', '.');
            double value;
            if (unit == Angle.Unit.DMS_DEGREE) {
                // Allow both signed DMS and DMS with direction
                value = normalized.matches(".*[NSEW]$")
                        ? DegMinSec.parseDMS(normalized)
                        : DegMinSec.parseSignedDMS(normalized);
            } else {
                value = Double.parseDouble(normalized);
            }

            double valueInDeg = unit.toUnit(value, Angle.Unit.DEGREE);
            if (valueInDeg < minDeg || valueInDeg > maxDeg) {
                throw new ValidationException(String.format("%s must be between %.1f° and %.1f°.", fieldName, minDeg, maxDeg));
            }
            return new Angle(value, unit);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Invalid angle format for " + fieldName + ": " + ex.getMessage());
        }
    }

    /**
    * Parses a required {@link Length} from a {@link TextField} and {@link ComboBox}.
     */
    @NotNull
    public static Length parseLength(TextField field, ComboBox<Length.Unit> unitCombo, String fieldName) throws ValidationException {
        Double value = parseDouble(field, fieldName, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
        return new Length(value, unitCombo.getValue());
    }

    /**
    * Parses an {@link Length} from a {@link TextField} and {@link ComboBox}, with optional support.
     */
    @Nullable
    public static Length parseLength(TextField field, ComboBox<Length.Unit> unitCombo, String fieldName, boolean isOptional) throws ValidationException {
        Double value = parseDouble(field, fieldName, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, isOptional);
        if (isOptional && value == null) return null;
        return new Length(value, unitCombo.getValue());
    }

    /**
    * Parses an optional {@link Pressure} from a {@link TextField} and {@link ComboBox}.
     */
    @Nullable
    public static Pressure parsePressure(TextField field, ComboBox<Pressure.Unit> unitCombo, String fieldName) throws ValidationException {
        Double value = parseDouble(field, fieldName, 0, Double.POSITIVE_INFINITY, true);
        if (value == null) return null;
        return new Pressure(value, unitCombo.getValue());
    }

    /**
    * Parses an optional {@link Temperature} from a {@link TextField} and {@link ComboBox}.
     */
    @Nullable
    public static Temperature parseTemperature(TextField field, ComboBox<Temperature.Unit> unitCombo, String fieldName) throws ValidationException {
        Double value = parseDouble(field, fieldName, -273.15, Double.POSITIVE_INFINITY, true);
        if (value == null) return null;
        return new Temperature(value, unitCombo.getValue());
    }

    /**
     * Parses a {@link Geographics.Geodetic} object from a set of fields.
     */
    @Nullable
    public static Geodetic parseGeodetic(TextField lonField, ComboBox<Angle.Unit> lonUnit, String lonName,
                                                TextField latField, ComboBox<Angle.Unit> latUnit, String latName,
                                                TextField altField, ComboBox<Length.Unit> altUnit, String altName) throws ValidationException {
        return parseGeodetic(lonField, lonUnit, lonName, latField, latUnit, latName, altField, altUnit, altName, false);
    }

    /**
     * Parses a {@link Geographics.Geodetic} object from a set of fields, with an option to allow empty fields.
     */
    @Nullable
    public static Geodetic parseGeodetic(TextField lonField, ComboBox<Angle.Unit> lonUnit, String lonName,
                                                TextField latField, ComboBox<Angle.Unit> latUnit, String latName,
                                                TextField altField, ComboBox<Length.Unit> altUnit, String altName,
                                                boolean isOptional) throws ValidationException {
        Angle lon = parseAngle(lonField, lonUnit, lonName, -180, 180, isOptional);
        Angle lat = parseAngle(latField, latUnit, latName, -90, 90, isOptional);
        Length alt = parseLength(altField, altUnit, altName, isOptional);

        if (isOptional && (lon == null || lat == null || alt == null)) {
            return null;
        }
        return new Geodetic(lon, lat, alt);
    }

    /**
     * Parses a {@link Geographics.Spherical} object from a set of fields.
     */
    @Nullable
    public static Spherical parseSpherical(TextField azField, ComboBox<Angle.Unit> azUnit, String azName,
                                                    TextField elField, ComboBox<Angle.Unit> elUnit, String elName,
                                                    TextField rangeField, ComboBox<Length.Unit> rangeUnit, String rangeName) throws ValidationException {
        Angle az = parseAngle(azField, azUnit, azName, 0, 360, false);
        Angle el = parseAngle(elField, elUnit, elName, -90, 90, false);
        Length range = parseLength(rangeField, rangeUnit, rangeName);
        return new Spherical(az, el, range);
    }

    /**
     * Parses a {@link Observation} object from a set of fields, with an option to allow empty fields.
     * An Observation consists of Azimuth, Range, and Altitude.
     *
     * @param azField     The text field for the azimuth value.
     * @param azUnit      The combo box for the azimuth unit.
     * @param azName      The name of the azimuth field for error messages.
     * @param rangeField  The text field for the range value.
     * @param rangeUnit   The combo box for the range unit.
     * @param rangeName   The name of the range field for error messages.
     * @param altField    The text field for the altitude value.
     * @param altUnit     The combo box for the altitude unit.
     * @param altName     The name of the altitude field for error messages.
     * @param isOptional  If true, blank inputs for all fields return null; otherwise, it throws an exception.
     * @return The parsed Observation object, or null if optional and any field is blank.
     * @throws ValidationException if validation fails for any of the fields.
     */
    @Nullable
    public static Observation parseObservation(TextField azField, ComboBox<Angle.Unit> azUnit, String azName,
        TextField rangeField, ComboBox<Length.Unit> rangeUnit, String rangeName, TextField altField,
        ComboBox<Length.Unit> altUnit, String altName, boolean isOptional) throws ValidationException {
        Angle az = parseAngle(azField, azUnit, azName, 0, 360, isOptional);
        Length range = parseLength(rangeField, rangeUnit, rangeName, isOptional);
        Length alt = parseLength(altField, altUnit, altName, isOptional);

        if (isOptional && (az == null || range == null || alt == null)) {
            return null;
        }
        return new Observation(az, range, alt);
    }
}