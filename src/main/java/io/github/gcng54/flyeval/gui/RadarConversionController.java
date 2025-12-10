package io.github.gcng54.flyeval.gui;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Locale;

import io.github.gcng54.flyeval.lib.units.*;
import io.github.gcng54.flyeval.lib.geods.*;
import io.github.gcng54.flyeval.lib.radars.*;
/**
 * Controller for the Radar Conversion tab, handling conversions between
 * spherical and geodetic coordinates.
 */
public class RadarConversionController {

    private enum InputMode {
        OBSERVATION_TO_GEODETIC("From Observation to Geodetic"),
        SPHERICAL_TO_GEODETIC("From Spherical to Geodetic"),
        GEODETIC_TO_SPHERICAL("From Geodetic to Spherical");

        private final String displayName;
        InputMode(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    private enum KFactorMode {
        MANUAL("Manual"),
        AUTO_STANDARD("Auto (Standard Atm.)");

        private final String displayName;
        KFactorMode(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }


    @FXML private ComboBox<InputMode> modeComboBox;

    // Radar Site
    @FXML private ComboBox<Angle.Unit> radarLonUnitCombo;
    @FXML private ComboBox<Angle.Unit> radarLatUnitCombo;
    @FXML private ComboBox<Length.Unit> radarAltUnitCombo;
    @FXML private TextField radarLonField;
    @FXML private TextField radarLatField;
    @FXML private TextField radarAltField;
    @FXML private ComboBox<KFactorMode> kFactorModeCombo;
    @FXML private TextField kFactorField;

    // Spherical Detection
    @FXML private Label detectionLabel;
    @FXML private ComboBox<Angle.Unit> azUnitCombo;
    @FXML private ComboBox<Angle.Unit> elUnitCombo;
    @FXML private ComboBox<Length.Unit> rangeUnitCombo;
    @FXML private ComboBox<Length.Unit> altUnitCombo;
    @FXML private TextField azField;
    @FXML private TextField elField;
    @FXML private TextField rangeField;
    @FXML private TextField altField;

    // Geodetic Target
    @FXML private ComboBox<Angle.Unit> tgtLonUnitCombo;
    @FXML private ComboBox<Angle.Unit> tgtLatUnitCombo;
    @FXML private ComboBox<Length.Unit> tgtAltUnitCombo;
    @FXML private TextField tgtLonField;
    @FXML private TextField tgtLatField;
    @FXML private TextField tgtAltField;

    @FXML private Label errorLabel;

    private boolean isUpdating;

    @FXML
    public void initialize() {
        initUnitCombos();
        modeComboBox.getItems().setAll(InputMode.values());
        modeComboBox.getSelectionModel().select(InputMode.OBSERVATION_TO_GEODETIC);
        modeComboBox.valueProperty().addListener((obs, o, n) -> {
            updateEnabledFields();
            handleCompute();
        });
        kFactorModeCombo.getItems().setAll(KFactorMode.values());
        kFactorModeCombo.getSelectionModel().select(KFactorMode.MANUAL);
        kFactorModeCombo.valueProperty().addListener((obs, o, n) -> {
            updateEnabledFields();
            handleCompute();
        });

        ChangeListener<String> listener = (obs, o, n) -> handleCompute();
        radarLonField.textProperty().addListener(listener);
        radarLatField.textProperty().addListener(listener);
        radarAltField.textProperty().addListener(listener);
        kFactorField.textProperty().addListener(listener);
        azField.textProperty().addListener(listener);
        elField.textProperty().addListener(listener);
        rangeField.textProperty().addListener(listener);
        altField.textProperty().addListener(listener);
        tgtLonField.textProperty().addListener(listener);
        tgtLatField.textProperty().addListener(listener);
        tgtAltField.textProperty().addListener(listener);

        ChangeListener<Object> unitListener = (obs, o, n) -> handleCompute();
        radarLonUnitCombo.valueProperty().addListener(unitListener);
        radarLatUnitCombo.valueProperty().addListener(unitListener);
        radarAltUnitCombo.valueProperty().addListener(unitListener);
        azUnitCombo.valueProperty().addListener(unitListener);
        elUnitCombo.valueProperty().addListener(unitListener);
        rangeUnitCombo.valueProperty().addListener(unitListener);
        altUnitCombo.valueProperty().addListener(unitListener);
        tgtLonUnitCombo.valueProperty().addListener(unitListener);
        tgtLatUnitCombo.valueProperty().addListener(unitListener);
        tgtAltUnitCombo.valueProperty().addListener(unitListener);

        radarLonField.setText("035 00 00 E");
        radarLatField.setText("39 00 00 N");
        radarAltField.setText("0");
        kFactorField.setText(String.format(Locale.ENGLISH, "%.6f", Radiations.STANDARD_REFRACTION_K));

        updateEnabledFields();
    }

    private void initUnitCombos() {
        Angle.Unit[] angleUnits = Angle.Unit.values();
        Length.Unit[] lengthUnits = Length.Unit.values();

        radarLonUnitCombo.getItems().setAll(angleUnits);
        radarLatUnitCombo.getItems().setAll(angleUnits);
        tgtLonUnitCombo.getItems().setAll(angleUnits);
        tgtLatUnitCombo.getItems().setAll(angleUnits);
        azUnitCombo.getItems().setAll(angleUnits);
        elUnitCombo.getItems().setAll(angleUnits);

        radarAltUnitCombo.getItems().setAll(lengthUnits);
        rangeUnitCombo.getItems().setAll(lengthUnits);
        tgtAltUnitCombo.getItems().setAll(lengthUnits);
        altUnitCombo.getItems().setAll(lengthUnits);

        radarLonUnitCombo.getSelectionModel().select(Angle.Unit.DMS_DEGREE);
        radarLatUnitCombo.getSelectionModel().select(Angle.Unit.DMS_DEGREE);
        tgtLonUnitCombo.getSelectionModel().select(Angle.Unit.DMS_DEGREE);
        tgtLatUnitCombo.getSelectionModel().select(Angle.Unit.DMS_DEGREE);
        azUnitCombo.getSelectionModel().select(Angle.Unit.DEGREE);
        elUnitCombo.getSelectionModel().select(Angle.Unit.DEGREE);

        radarAltUnitCombo.getSelectionModel().select(Length.Unit.METER);
        rangeUnitCombo.getSelectionModel().select(Length.Unit.KILOMETER);
        tgtAltUnitCombo.getSelectionModel().select(Length.Unit.METER);
        altUnitCombo.getSelectionModel().select(Length.Unit.METER);
    }

    private void updateEnabledFields() {
        InputMode mode = modeComboBox.getValue();

        // Update the title of the detection section based on the mode.
        if (mode == InputMode.OBSERVATION_TO_GEODETIC) {
            detectionLabel.setText("Observation Detection");
        } else {
            detectionLabel.setText("Spherical Detection");
        }

        // Default all to disabled, then enable based on mode.
        setGroupEditable(false, azField, elField, rangeField, altField, tgtLonField, tgtLatField, tgtAltField);

        // Handle k-Factor field editability
        boolean kFactorManual = kFactorModeCombo.getValue() == KFactorMode.MANUAL;
        kFactorField.setEditable(kFactorManual);

        switch (mode) {
            case OBSERVATION_TO_GEODETIC ->
                // Input: Az, Range, Alt. Output: Geodetic Target & Elevation.
                setGroupEditable(true, azField, rangeField, altField);
            case SPHERICAL_TO_GEODETIC ->
                // Input: Az, El, Range. Output: Geodetic Target & Altitude.
                setGroupEditable(true, azField, elField, rangeField);
            case GEODETIC_TO_SPHERICAL ->
                // Input: Geodetic Target. Output: Az, El, Range, Alt.
                setGroupEditable(true, tgtLonField, tgtLatField, tgtAltField);
        }
    }

    private void setGroupEditable(boolean editable, TextField... fields) {
        for (TextField tf : fields) {
            tf.setEditable(editable);
            tf.setStyle(editable ? "" : "-fx-control-inner-background: #f0f0f0;");
        }
    }

    @FXML
    private void handleCompute() {
        if (isUpdating) return;

        isUpdating = true;
        try {
            errorLabel.setText("");
            Geodetic radarSite = FormParserUtils.parseGeodetic(
                    radarLonField, radarLonUnitCombo, "Radar Longitude",
                    radarLatField, radarLatUnitCombo, "Radar Latitude",
                    radarAltField, radarAltUnitCombo, "Radar Altitude");

            if (radarSite == null) {
                // Do nothing if primary inputs are invalid, wait for user correction.
                return;
            }

            double kFactor = getKFactor(radarSite);

            switch (modeComboBox.getValue()) {
                case OBSERVATION_TO_GEODETIC -> computeGeodeticFromObservation(radarSite, kFactor);
                case SPHERICAL_TO_GEODETIC -> computeGeodeticFromSpherical(radarSite, kFactor);
                case GEODETIC_TO_SPHERICAL -> computeSphericalFromGeodetic(radarSite, kFactor);
            }
        } catch (FormParserUtils.ValidationException e) {
            errorLabel.setText("Input Error: " + e.getMessage());
        } finally {
            isUpdating = false;
        }
    }

    private double getKFactor(Geodetic radarSite) throws FormParserUtils.ValidationException {
        if (kFactorModeCombo.getValue() == KFactorMode.AUTO_STANDARD) {
            Geodetic target = FormParserUtils.parseGeodetic(tgtLonField, tgtLonUnitCombo, "Target Lon", tgtLatField, tgtLatUnitCombo, "Target Lat", tgtAltField, tgtAltUnitCombo, "Target Alt", true);
            Length targetAlt = (target != null) ? target.alt() : Length.fromAltitudeMeter(0); // Default to 0 if not available
            double k = Refraction.calculateKFactorFromStandardAtmosphere(radarSite.alt(), targetAlt);
            kFactorField.setText(String.format(Locale.ENGLISH, "%.6f", k));
            return k;
        }
        // Manual mode
        return FormParserUtils.parseDouble(kFactorField, "k-Factor", 0, 1000, false);
    }

    private Observation parseObservation() throws FormParserUtils.ValidationException {
        Angle az = FormParserUtils.parseAngle(azField, azUnitCombo, "Azimuth", 0, 360, false);
        Length range = FormParserUtils.parseLength(rangeField, rangeUnitCombo, "Slant Range");
        Length alt = FormParserUtils.parseLength(altField, altUnitCombo, "Altitude");
        if (az == null || range == null || alt == null) return null;
        return new Observation(az, range, alt);
    }

    private Spherical parseSpherical() throws FormParserUtils.ValidationException {
        Angle az = FormParserUtils.parseAngle(azField, azUnitCombo, "Azimuth", 0, 360, false);
        Angle el = FormParserUtils.parseAngle(elField, elUnitCombo, "Elevation", -90, 90, false);
        Length range = FormParserUtils.parseLength(rangeField, rangeUnitCombo, "Slant Range");
        if (az == null || el == null || range == null) return null; // Should be caught by ValidationException
        return new Spherical(az, el, range);
    }

    private void computeGeodeticFromObservation(Geodetic radarSite, double kFactor) throws FormParserUtils.ValidationException {
        Observation observation = parseObservation();
        if (observation == null) {
            clearTargetGeodetic();
            return;
        }
        // This is a two-step conversion. First, get Spherical to find the elevation.
        Spherical spherical = Radiations.toSpherical(radarSite, observation, kFactor);
        // Then, use the full spherical data to get the final geodetic position.
        Geodetic target = Radiations.toGeodetic(radarSite, spherical, kFactor);

        writeTargetGeodetic(target);
        writeSpherical(spherical); // Also write the calculated elevation
    }

    private void computeGeodeticFromSpherical(Geodetic radarSite, double kFactor) throws FormParserUtils.ValidationException {
        Spherical detection = parseSpherical();
        if (detection == null) {
            clearTargetGeodetic();
            return;
        }
        Geodetic target = Radiations.toGeodetic(radarSite, detection, kFactor);
        writeTargetGeodetic(target);
        writeAltitude(target.alt()); // Also write the calculated altitude
    }

    private void computeSphericalFromGeodetic(Geodetic radarSite, double kFactor) throws FormParserUtils.ValidationException {
        Geodetic target = FormParserUtils.parseGeodetic(
                tgtLonField, tgtLonUnitCombo, "Target Longitude",
                tgtLatField, tgtLatUnitCombo, "Target Latitude",
                tgtAltField, tgtAltUnitCombo, "Target Altitude");
        if (target == null) {
            clearSpherical();
            return;
        }
        Spherical spherical = Radiations.toSpherical(radarSite, target, kFactor);
        writeSpherical(spherical);
        writeAltitude(target.alt()); // Copy the target's altitude to the detection altitude field
    }

    private void writeTargetGeodetic(Geodetic geo) {
        Angle.Unit lonU = tgtLonUnitCombo.getValue();
        Angle.Unit latU = tgtLatUnitCombo.getValue();
        Length.Unit altU = tgtAltUnitCombo.getValue() == null ? Length.Unit.METER : tgtAltUnitCombo.getValue();
        setAngleField(tgtLonField, geo.lon(), lonU); // Longitude
        setAngleField(tgtLatField, geo.lat(), latU); // Latitude
        tgtAltField.setText(String.format(Locale.ENGLISH, "%.2f", geo.alt().create(altU).getValue())); // Altitude
    }

    private void writeSpherical(Spherical sph) {
        Angle.Unit azU = azUnitCombo.getValue();
        Angle.Unit elU = elUnitCombo.getValue();
        Length.Unit rU = rangeUnitCombo.getValue();
        setAngleField(azField, sph.azimuth(), azU);
        setAngleField(elField, sph.elevation(), elU);
        rangeField.setText(String.format(Locale.ENGLISH, "%.3f", sph.range().create(rU).getValue()));
    }

    private void writeAltitude(Length alt) {
        Length.Unit altU = altUnitCombo.getValue();
        altField.setText(String.format(Locale.ENGLISH, "%.2f", alt.create(altU).getValue()));
    }

    private void setAngleField(TextField field, Angle angle, Angle.Unit unit) {
        Angle displayAngle = angle.create(unit);
        if (displayAngle.getUnit() == Angle.Unit.DMS_DEGREE) {
            field.setText(displayAngle.toString());
        } else {
            field.setText(String.format(Locale.ENGLISH, "%.6f", displayAngle.getValue()));
        }
    }

    private void clearAllDerived() {
        clearSpherical();
        clearTargetGeodetic();
    }

    private void clearSpherical() { azField.clear(); elField.clear(); rangeField.clear(); altField.clear(); }
    private void clearTargetGeodetic() { tgtLonField.clear(); tgtLatField.clear(); tgtAltField.clear(); }
}