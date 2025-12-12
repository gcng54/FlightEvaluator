package io.github.gcng54.flyeval.gui;

import javafx.fxml.FXML;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Locale;

import io.github.gcng54.flyeval.lib.units.*;
import io.github.gcng54.flyeval.lib.geods.*;

/**
 * Controller for geographic conversions between geodetic, cartesian (ECEF
 * offset), and spherical (ENU) representations.
 */
public class GeoConversionController {

    private enum InputMode {
        TARGET_GEODETIC, CARTESIAN_OFFSET, SPHERICAL_OFFSET
    }

    @FXML
    private ComboBox<InputMode> modeComboBox;

    @FXML
    private ComboBox<Angle.Unit> refLonUnitCombo;
    @FXML
    private ComboBox<Angle.Unit> refLatUnitCombo;
    @FXML
    private ComboBox<Length.Unit> refAltUnitCombo;

    @FXML
    private TextField refLonField;
    @FXML
    private TextField refLatField;
    @FXML
    private TextField refAltField;

    @FXML
    private ComboBox<Angle.Unit> tgtLonUnitCombo;
    @FXML
    private ComboBox<Angle.Unit> tgtLatUnitCombo;
    @FXML
    private ComboBox<Length.Unit> tgtAltUnitCombo;
    @FXML
    private TextField tgtLonField;
    @FXML
    private TextField tgtLatField;
    @FXML
    private TextField tgtAltField;

    @FXML
    private ComboBox<Length.Unit> dxUnitCombo;
    @FXML
    private ComboBox<Length.Unit> dyUnitCombo;
    @FXML
    private ComboBox<Length.Unit> dzUnitCombo;
    @FXML
    private TextField dxField;
    @FXML
    private TextField dyField;
    @FXML
    private TextField dzField;

    @FXML
    private ComboBox<Angle.Unit> azUnitCombo;
    @FXML
    private ComboBox<Angle.Unit> elUnitCombo;
    @FXML
    private ComboBox<Length.Unit> rangeUnitCombo;
    @FXML
    private TextField azField;
    @FXML
    private TextField elField;
    @FXML
    private TextField rangeField;

    @FXML
    private Label errorLabel;

    private boolean isUpdating;

    @FXML
    public void initialize() {
        initUnitCombos();
        modeComboBox.getItems().setAll(InputMode.values());
        modeComboBox.getSelectionModel().select(InputMode.TARGET_GEODETIC);
        modeComboBox.valueProperty().addListener((obs, o, n) -> {
            updateEnabledFields();
            handleCompute();
        });

        // Add listeners to all input fields to trigger computation on change.
        ChangeListener<Object> listener = (obs, o, n) -> handleCompute();
        refLonField.textProperty().addListener(listener);
        refLatField.textProperty().addListener(listener);
        refAltField.textProperty().addListener(listener);
        tgtLonField.textProperty().addListener(listener);
        tgtLatField.textProperty().addListener(listener);
        tgtAltField.textProperty().addListener(listener);
        dxField.textProperty().addListener(listener);
        dyField.textProperty().addListener(listener);
        dzField.textProperty().addListener(listener);
        azField.textProperty().addListener(listener);
        elField.textProperty().addListener(listener);
        rangeField.textProperty().addListener(listener);

        refLonUnitCombo.valueProperty().addListener(listener);
        refLatUnitCombo.valueProperty().addListener(listener);
        refAltUnitCombo.valueProperty().addListener(listener);
        tgtLonUnitCombo.valueProperty().addListener(listener);
        tgtLatUnitCombo.valueProperty().addListener(listener);
        tgtAltUnitCombo.valueProperty().addListener(listener);

        updateEnabledFields();
    }

    private void initUnitCombos() {
        Angle.Unit[] angleUnits = Angle.Unit.values();
        Length.Unit[] lengthUnits = Length.Unit.values();

        refLonUnitCombo.getItems().setAll(angleUnits);
        refLatUnitCombo.getItems().setAll(angleUnits);
        tgtLonUnitCombo.getItems().setAll(angleUnits);
        tgtLatUnitCombo.getItems().setAll(angleUnits);
        azUnitCombo.getItems().setAll(angleUnits);
        elUnitCombo.getItems().setAll(angleUnits);

        refAltUnitCombo.getItems().setAll(lengthUnits);
        tgtAltUnitCombo.getItems().setAll(lengthUnits);
        dxUnitCombo.getItems().setAll(lengthUnits);
        dyUnitCombo.getItems().setAll(lengthUnits);
        dzUnitCombo.getItems().setAll(lengthUnits);
        rangeUnitCombo.getItems().setAll(lengthUnits);

        refLonUnitCombo.getSelectionModel().select(Angle.Unit.DMS_DEGREE);
        refLatUnitCombo.getSelectionModel().select(Angle.Unit.DMS_DEGREE);
        tgtLonUnitCombo.getSelectionModel().select(Angle.Unit.DMS_DEGREE);
        tgtLatUnitCombo.getSelectionModel().select(Angle.Unit.DMS_DEGREE);
        azUnitCombo.getSelectionModel().select(Angle.Unit.DEGREE);
        elUnitCombo.getSelectionModel().select(Angle.Unit.DEGREE);

        refAltUnitCombo.getSelectionModel().select(Length.Unit.METER);
        tgtAltUnitCombo.getSelectionModel().select(Length.Unit.METER);
        dxUnitCombo.getSelectionModel().select(Length.Unit.KILOMETER);
        dyUnitCombo.getSelectionModel().select(Length.Unit.KILOMETER);
        dzUnitCombo.getSelectionModel().select(Length.Unit.KILOMETER);
        rangeUnitCombo.getSelectionModel().select(Length.Unit.KILOMETER);
    }

    private void updateEnabledFields() {
        InputMode mode = modeComboBox.getValue();
        boolean geoEnabled = mode == InputMode.TARGET_GEODETIC;
        boolean cartEnabled = mode == InputMode.CARTESIAN_OFFSET;
        boolean sphEnabled = mode == InputMode.SPHERICAL_OFFSET;

        setGroupEnabled(geoEnabled, tgtLonField, tgtLatField, tgtAltField);
        setGroupEnabled(cartEnabled, dxField, dyField, dzField);
        setGroupEnabled(sphEnabled, azField, elField, rangeField);
    }

    private void setGroupEnabled(boolean enabled, TextField... fields) {
        for (TextField tf : fields) {
            tf.setDisable(false); // Always enabled for focus and input
            tf.setEditable(enabled); // Only allow editing if enabled
            tf.setStyle(enabled ? "" : "-fx-opacity: 0.7;");
        }
    }

    @FXML
    private void handleCompute() {
        if (isUpdating)
            return;
        isUpdating = true;
        try {
            errorLabel.setText("");
            Geodetic reference = FormParserUtils.parseGeodetic(
                    refLonField, refLonUnitCombo, "Reference Longitude",
                    refLatField, refLatUnitCombo, "Reference Latitude",
                    refAltField, refAltUnitCombo, "Reference Altitude");

            InputMode mode = modeComboBox.getValue();
            if (mode == null)
                mode = InputMode.TARGET_GEODETIC;

            if (reference == null) {
                // Keep target inputs intact when they are the source (TARGET_GEODETIC).
                if (mode == InputMode.TARGET_GEODETIC) {
                    clearDerivedCartesian();
                    clearDerivedSpherical();
                } else {
                    clearDerived();
                }
                return;
            }

            switch (mode) {
                case TARGET_GEODETIC -> computeFromTargetGeodetic(reference);
                case CARTESIAN_OFFSET -> computeFromCartesian(reference);
                case SPHERICAL_OFFSET -> computeFromSpherical(reference);
            }
        } catch (FormParserUtils.ValidationException e) {
            errorLabel.setText("Input Error: " + e.getMessage());
        } finally {
            isUpdating = false;
        }
    }

    private void computeFromTargetGeodetic(Geodetic reference) throws FormParserUtils.ValidationException {
        Geodetic target = FormParserUtils.parseGeodetic(
                tgtLonField, tgtLonUnitCombo, "Target Longitude",
                tgtLatField, tgtLatUnitCombo, "Target Latitude",
                tgtAltField, tgtAltUnitCombo, "Target Altitude");

        if (target == null) {
            clearDerived();
            return;
        }
        Cartesian ecefDisp = reference.getCartesian(target);
        Cartesian enuDisp = reference.toENU(ecefDisp);
        Spherical sph = reference.getSpherical(target);
        writeCartesian(enuDisp);
        writeSpherical(sph);
    }

    private void computeFromCartesian(Geodetic reference) throws FormParserUtils.ValidationException {
        Cartesian enuDisp = parseCartesian();
        if (enuDisp == null) {
            clearDerivedSpherical();
            clearTargetGeodetic();
            return;
        }
        Cartesian ecefDisp = reference.toECEF(enuDisp);
        Geodetic target = reference.transform(ecefDisp);
        writeTargetGeodetic(target);
        Spherical sph = enuDisp.toSpherical();
        writeSpherical(sph);
    }

    private void computeFromSpherical(Geodetic reference) throws FormParserUtils.ValidationException {
        Spherical sph = parseSpherical();
        if (sph == null) {
            clearDerivedCartesian();
            clearTargetGeodetic();
            return;
        }
        Cartesian enuDisp = (Cartesian) sph.toCartesian();
        Cartesian ecefDisp = reference.toECEF(enuDisp);
        Geodetic target = reference.transform(ecefDisp);
        writeTargetGeodetic(target);
        writeCartesian(enuDisp);
        writeSpherical(enuDisp.toSpherical());
    }

    private Cartesian parseCartesian() throws FormParserUtils.ValidationException {
        Double dx = FormParserUtils.parseDouble(dxField, "dx", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                false);
        Double dy = FormParserUtils.parseDouble(dyField, "dy", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                false);
        Double dz = FormParserUtils.parseDouble(dzField, "dz", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                false);
        if (dx == null || dy == null || dz == null)
            return null;
        return new Cartesian(
                Length.ofLength(dx, dxUnitCombo.getValue() == null ? Length.Unit.KILOMETER : dxUnitCombo.getValue()),
                Length.ofLength(dy, dyUnitCombo.getValue() == null ? Length.Unit.KILOMETER : dyUnitCombo.getValue()),
                Length.ofLength(dz, dzUnitCombo.getValue() == null ? Length.Unit.KILOMETER : dzUnitCombo.getValue()));
    }

    private Spherical parseSpherical() throws FormParserUtils.ValidationException {
        Angle az = FormParserUtils.parseAngle(azField, azUnitCombo, "Azimuth", 0, 360, false);
        Angle el = FormParserUtils.parseAngle(elField, elUnitCombo, "Elevation", -90, 90, false);
        Double rangeVal = FormParserUtils.parseDouble(rangeField, "Range", 0, Double.MAX_VALUE, false);

        if (az == null || el == null || rangeVal == null)
            return null;

        Length range = Length.ofLength(rangeVal,
                rangeUnitCombo.getValue() == null ? Length.Unit.KILOMETER : rangeUnitCombo.getValue());

        if (az == null || el == null || range == null)
            return null;
        return new Spherical(az, el, range);
    }

    private void writeTargetGeodetic(Geodetic geo) {
        Angle.Unit lonU = tgtLonUnitCombo.getValue() == null ? Angle.Unit.DEGREE : tgtLonUnitCombo.getValue();
        Angle.Unit latU = tgtLatUnitCombo.getValue() == null ? Angle.Unit.DEGREE : tgtLatUnitCombo.getValue();
        Length.Unit altU = tgtAltUnitCombo.getValue() == null ? Length.Unit.METER : tgtAltUnitCombo.getValue();
        setAngleField(tgtLonField, geo.lon(), lonU);
        setAngleField(tgtLatField, geo.lat(), latU);
        setIfChanged(tgtAltField, geo.alt().create(altU).getValue(), "%.2f");
    }

    private void writeCartesian(Cartesian cart) {
        setIfChanged(dxField, cart.getX()
                .create(dxUnitCombo.getValue() == null ? Length.Unit.KILOMETER : dxUnitCombo.getValue()).getValue(),
                "%.3f");
        setIfChanged(dyField, cart.getY()
                .create(dyUnitCombo.getValue() == null ? Length.Unit.KILOMETER : dyUnitCombo.getValue()).getValue(),
                "%.3f");
        setIfChanged(dzField, cart.getZ()
                .create(dzUnitCombo.getValue() == null ? Length.Unit.KILOMETER : dzUnitCombo.getValue()).getValue(),
                "%.3f");
    }

    private void writeSpherical(Spherical sph) {
        Angle.Unit azU = azUnitCombo.getValue() == null ? Angle.Unit.DEGREE : azUnitCombo.getValue();
        Angle.Unit elU = elUnitCombo.getValue() == null ? Angle.Unit.DEGREE : elUnitCombo.getValue();
        Length.Unit rU = rangeUnitCombo.getValue() == null ? Length.Unit.KILOMETER : rangeUnitCombo.getValue();
        setAngleField(azField, sph.getAzimuth(), azU);
        setAngleField(elField, sph.getElevation(), elU);
        setIfChanged(rangeField, sph.getRange().create(rU).getValue(), "%.3f");
    }

    private void setIfChanged(TextField field, double value, String format) {
        if (field.isEditable() && field.isFocused())
            return; // Don't overwrite user input in editable fields while focused
        String formatted = String.format(Locale.ENGLISH, format, value);
        if (!formatted.equals(field.getText())) {
            field.setText(formatted);
        }
    }

    private void setIfChanged(TextField field, String value) {
        if (field.isEditable() && field.isFocused())
            return;
        if (!value.equals(field.getText())) {
            field.setText(value);
        }
    }

    private void setAngleField(TextField field, Angle angle, Angle.Unit unit) {
        Angle displayAngle = angle.create(unit == null ? Angle.Unit.DMS_DEGREE : unit);
        if (displayAngle.getUnit() == Angle.Unit.DMS_DEGREE) {
            setIfChanged(field, displayAngle.toString());
        } else {
            setIfChanged(field, displayAngle.getValue(), "%.6f");
        }
    }

    private void clearDerived() {
        clearDerivedCartesian();
        clearDerivedSpherical();
        clearTargetGeodetic();
    }

    private void clearTargetGeodetic() {
        tgtLonField.clear();
        tgtLatField.clear();
        tgtAltField.clear();
    }

    private void clearDerivedCartesian() {
        dxField.clear();
        dyField.clear();
        dzField.clear();
    }

    private void clearDerivedSpherical() {
        azField.clear();
        elField.clear();
        rangeField.clear();
    }
}
