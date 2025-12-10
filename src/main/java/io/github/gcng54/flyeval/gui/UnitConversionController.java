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

public class UnitConversionController {
    // Close button now handled globally in main window

    @FXML private ComboBox<EDimension> dimensionComboBox;
    @FXML private ComboBox<IUnit<?>> sourceUnitComboBox;
    @FXML private ComboBox<IUnit<?>> targetUnitComboBox;
    @FXML private TextField sourceValueField;
    @FXML private TextField targetValueField;
    @FXML private Label errorLabel;

    // --- Refraction UI Elements ---
    @FXML private TextField siteAltField;
    @FXML private ComboBox<Length.Unit> siteAltUnitCombo;
    @FXML private TextField targetAltField;
    @FXML private ComboBox<Length.Unit> targetAltUnitCombo;
    @FXML private TextField sitePressureField;
    @FXML private ComboBox<Pressure.Unit> sitePressureUnitCombo;
    @FXML private TextField siteTempField;
    @FXML private ComboBox<Temperature.Unit> siteTempUnitCombo;
    @FXML private TextField siteRhField;
    @FXML private TextField kFactorResultField;
    @FXML private Label kFactorErrorLabel;


    @FXML
    public void initialize() {
        // 1. Populate the dimension combo box
        dimensionComboBox.getItems().setAll(EDimension.values());

        // 2. Add listeners to handle dimension selection
        dimensionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldDim, newDim) -> {
            if (newDim != null) {
                populateUnitComboBoxes(newDim);
            }
        });

        // 3. Set default dimension
        dimensionComboBox.getSelectionModel().selectFirst();

        // 4. Add listeners for automatic conversion
        ChangeListener<Object> autoConversionListener = (obs, oldVal, newVal) -> handleConvert();
        sourceValueField.textProperty().addListener(autoConversionListener);
        sourceUnitComboBox.valueProperty().addListener(autoConversionListener);
        targetUnitComboBox.valueProperty().addListener(autoConversionListener);

        // Set initial value
        sourceValueField.setText("1");

        initializeRefractionTab();
    }

    private void populateUnitComboBoxes(EDimension dimension) {
        IUnit<?>[] units = switch (dimension) {
            case LENGTH -> Length.Unit.values();
            case ANGLE -> Angle.Unit.values();
            case TIME -> Time.Unit.values();
            case SPEED -> Speed.Unit.values();
            case AREA -> Area.Unit.values();
            case VOLUME -> Volume.Unit.values();
            case PRESSURE -> Pressure.Unit.values();
            case TEMPERATURE -> Temperature.Unit.values();
        };

        sourceUnitComboBox.getItems().setAll(units);
        targetUnitComboBox.getItems().setAll(units);

        if (units.length > 0) {
            if (dimension == EDimension.LENGTH) {
                sourceUnitComboBox.getSelectionModel().select(Length.Unit.KILOMETER);
                targetUnitComboBox.getSelectionModel().select(Length.Unit.KILOMETER);
            } else if (dimension == EDimension.ANGLE) {
                sourceUnitComboBox.getSelectionModel().select(Angle.Unit.DMS_DEGREE);
                targetUnitComboBox.getSelectionModel().select(Angle.Unit.DMS_DEGREE);
            } else {
                sourceUnitComboBox.getSelectionModel().selectFirst();
                int targetIndex = units.length > 1 ? 1 : 0;
                targetUnitComboBox.getSelectionModel().select(targetIndex);
            }
        } else {
            sourceUnitComboBox.getSelectionModel().clearSelection();
            targetUnitComboBox.getSelectionModel().clearSelection();
        }

        // Refresh conversion with the newly selected units
        handleConvert();
    }

    @FXML
    private void handleConvert() {
        errorLabel.setText(""); // Clear previous errors
        try {
            String sourceText = sourceValueField.getText();
            if (sourceText == null || sourceText.isBlank()) {
                targetValueField.clear();
                return;
            }

            double sourceValue = parseValue(sourceText, sourceUnitComboBox.getValue());
            IUnit<?> sourceUnit = sourceUnitComboBox.getValue();
            IUnit<?> targetUnit = targetUnitComboBox.getValue();

            if (sourceUnit == null || targetUnit == null) {
                return; // Not ready to convert yet
            }

            if (!sourceUnit.isCompatible(targetUnit)) {
                errorLabel.setText("Incompatible units selected.");
                targetValueField.clear();
                return;
            }

            double baseValue = sourceUnit.toBase(sourceValue);
            double targetValue = targetUnit.fromBase(baseValue);

            if (targetUnit instanceof Angle.Unit angleUnit && angleUnit == Angle.Unit.DMS_DEGREE) {
                Angle angle = new Angle(targetValue, angleUnit);
                targetValueField.setText(angle.toString());
            } else {
                targetValueField.setText(String.format(Locale.ENGLISH, "%.6f", targetValue));
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid input. Please enter a number or DMS value.");
            targetValueField.clear();
        } catch (Exception e) {
            errorLabel.setText("An error occurred during conversion.");
            e.printStackTrace();
        }
    }

    private double parseValue(String text, IUnit<?> unit) {
        if (text == null || text.isBlank()) {
            throw new NumberFormatException("Empty input");
        }

        String normalized = text.trim().replace(',', '.');
        if (unit instanceof Angle.Unit angleUnit && angleUnit == Angle.Unit.DMS_DEGREE) {
            return DegMinSec.parseSignedDMS(normalized);
        }
        return Double.parseDouble(normalized);
    }

    private void initializeRefractionTab() {
        // Populate unit combo boxes
        siteAltUnitCombo.getItems().setAll(Length.Unit.values());
        targetAltUnitCombo.getItems().setAll(Length.Unit.values());
        sitePressureUnitCombo.getItems().setAll(Pressure.Unit.values());
        siteTempUnitCombo.getItems().setAll(Temperature.Unit.values());

        // Set default selections
        siteAltUnitCombo.getSelectionModel().select(Length.Unit.METER);
        targetAltUnitCombo.getSelectionModel().select(Length.Unit.METER);
        sitePressureUnitCombo.getSelectionModel().select(Pressure.Unit.HECTOPASCAL);
        siteTempUnitCombo.getSelectionModel().select(Temperature.Unit.CELSIUS);
    }

    @FXML
    private void handleCalculateKFactor() {
        kFactorErrorLabel.setText("");
        try {
            // Parse required altitude values
            Length siteAlt = FormParserUtils.parseLength(siteAltField, siteAltUnitCombo, "Site Altitude");
            Length targetAlt = FormParserUtils.parseLength(targetAltField, targetAltUnitCombo, "Target Altitude");

            // Parse optional weather values
            Pressure sitePressure = FormParserUtils.parsePressure(sitePressureField, sitePressureUnitCombo, "Site Pressure");
            Temperature siteTemp = FormParserUtils.parseTemperature(siteTempField, siteTempUnitCombo, "Site Temperature");
            Double siteRh = FormParserUtils.parseDouble(siteRhField, "Site Rel. Humidity", 0, 100, true);

            boolean hasWeather = sitePressure != null && siteTemp != null && siteRh != null;

            double kFactor;
            if (hasWeather) {
                // Calculate with site weather, assuming standard atmosphere at target
                kFactor = Refraction.calculateKFactorFromAtmosphericProfile(siteAlt, sitePressure, siteTemp, siteRh, targetAlt);
            } else {
                // Calculate with standard atmosphere for both points
                kFactor = Refraction.calculateKFactorFromStandardAtmosphere(siteAlt, targetAlt);
            }

            kFactorResultField.setText(String.format(Locale.ENGLISH, "%.6f", kFactor));

        } catch (FormParserUtils.ValidationException e) {
            kFactorErrorLabel.setText("Input Error: " + e.getMessage());
        } catch (Exception e) {
            kFactorErrorLabel.setText("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}