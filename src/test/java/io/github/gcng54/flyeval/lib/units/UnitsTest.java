package io.github.gcng54.flyeval.lib.units;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Nested;

public class UnitsTest {

    @Nested
    public class EDimensionsTest {

        @Test
        public void testPhysicalDimensionFromNameCaseInsensitive() {
            assertEquals(EDimensions.LENGTH, EDimensions.fromName("length"));
            assertEquals(EDimensions.ANGLE, EDimensions.fromName("AnGlE"));
            assertEquals(EDimensions.TIME, EDimensions.fromName("TIME"));
        }

        @Test
        public void testPhysicalDimensionFromNameInvalid() {
            assertThrows(IllegalArgumentException.class, () -> EDimensions.fromName("not_a_dimension"));
        }

        @Test
        public void testPhysicalDimensionBaseUnits() {
            assertEquals(Length.Unit.METER, EDimensions.LENGTH.getBaseUnit());
            assertEquals(Angle.Unit.RADIAN, EDimensions.ANGLE.getBaseUnit());
            assertEquals(Time.Unit.SECOND, EDimensions.TIME.getBaseUnit());
            assertEquals(Speed.Unit.METER_HR, EDimensions.SPEED.getBaseUnit());
            assertEquals(Area.Unit.SQ_METER, EDimensions.AREA.getBaseUnit());
            assertEquals(Volume.Unit.CU_METER, EDimensions.VOLUME.getBaseUnit());
            assertEquals(Pressure.Unit.PASCAL, EDimensions.PRESSURE.getBaseUnit());
            assertEquals(Temperature.Unit.KELVIN, EDimensions.TEMPERATURE.getBaseUnit());
        }

        @Test
        public void testPhysicalDimensionToStringIsNiceName() {
            assertEquals("Length", EDimensions.LENGTH.toString());
            assertEquals("Angle", EDimensions.ANGLE.toString());
            assertEquals("Time", EDimensions.TIME.toString());
        }

        @Test
        public void testIUnitCompatibilitySameEnum() {
            Length.Unit m = Length.Unit.METER;
            Length.Unit km = Length.Unit.KILOMETER;
            assertTrue(m.isCompatible(km));
        }

        @Test
        public void testIUnitCompatibilityDifferentEnum() {
            Length.Unit m = Length.Unit.METER;
            Area.Unit m2 = Area.Unit.SQ_METER;
            assertFalse(m.isCompatible(m2));
        }

        @Test
        public void testIUnitConvertBetweenSameClass() {
            Length.Unit m = Length.Unit.METER;
            Length.Unit km = Length.Unit.KILOMETER;

            double kmValue = 1.0;
            double meters = km.toBase(kmValue); // 1000 m
            assertEquals(1000.0, meters, 1e-9);

            double backToKm = km.fromBase(meters);
            assertEquals(kmValue, backToKm, 1e-9);

            double viaConvert = m.convert(kmValue, km, m);
            assertEquals(1000.0, viaConvert, 1e-9);
        }

        @Test
        public void testIUnitConvertSameUnitReturnsOriginal() {
            Length.Unit m = Length.Unit.METER;
            double value = 123.45;
            assertEquals(value, m.convert(value, m, m), 0.0);
        }

        @Test
        public void testValidateFactorRejectsNonPositive() {
            // Use anonymous implementation to call validateFactor directly
            assertThrows(IllegalArgumentException.class, () -> {
            IUnit<?> unit = new IUnit<Length.Unit>() {
                @Override public String getSymbol() { return "x"; }
                @Override public double getFactor() { return 1.0; }
            };
            unit.validateFactor(0.0);
            });
        }

        @Test
        public void testELengthsToStringSpecialCases() {
            assertEquals("Feet", Length.Unit.FOOT.toString());
            assertEquals("Inches", Length.Unit.INCH.toString());
            assertEquals("FlightLevel", Length.Unit.FLIGHTLEVEL.toString());
        }

        @Test
        public void testELengthsDerivedUnitsHelpers() {
            assertEquals(Area.Unit.SQ_METER, Length.Unit.METER.getAreaUnit());
            assertEquals(Volume.Unit.CU_METER, Length.Unit.METER.getVolumeUnit());
            assertEquals(Speed.Unit.METER_HR, Length.Unit.METER.getSpeedUnit());
        }

        @Test
        public void testEAnglesConversionDegreeRadianRoundTrip() {
            double angleDeg = 180.0;
            double rad = Angle.Unit.DEGREE.toBase(angleDeg);
            assertEquals(Math.PI, rad, 1e-12);
            double backDeg = Angle.Unit.DEGREE.fromBase(rad);
            assertEquals(angleDeg, backDeg, 1e-9);
        }

        @Test
        public void testETimesConversionHourSecond() {
            double hours = 1.5;
            double seconds = Time.Unit.HOUR.toBase(hours);
            assertEquals(5400.0, seconds, 1e-9);
            double backHours = Time.Unit.HOUR.fromBase(seconds);
            assertEquals(hours, backHours, 1e-9);
        }

        @Test
        public void testESpeedsIsMetersPerSecondBase() {
            Speed.Unit kmh = Speed.Unit.KILOMETER_HR;
            // 1 km/h = 1000 / 3600 m/s
            assertEquals(1000.0 / 3600.0, kmh.getFactor(), 1e-12);
            assertEquals(Length.Unit.KILOMETER, kmh.getLengthUnit());
        }

        @Test
        public void testEAreasIsSquareOfLengthFactor() {
            Length.Unit km = Length.Unit.KILOMETER;
            Area.Unit sqKm = Area.Unit.SQ_KILOMETER;
            assertEquals(Math.pow(km.getFactor(), 2), sqKm.getFactor(), 1e-6);
            assertEquals(km, sqKm.getLengthUnit());
        }

        @Test
        public void testEVolumesIsCubeOfLengthFactor() {
            Length.Unit km = Length.Unit.KILOMETER;
            Volume.Unit cuKm = Volume.Unit.CU_KILOMETER;
            assertEquals(Math.pow(km.getFactor(), 3), cuKm.getFactor(), 1e-12);
            assertEquals(km, cuKm.getLengthUnit());
        }

        @Test
        public void testEPressuresConversion() {
            assertEquals(100.0, Pressure.Unit.HECTOPASCAL.getFactor(), 1e-9);
            assertEquals(100000.0, Pressure.Unit.BAR.getFactor(), 1e-9);
            assertEquals(100.0, Pressure.Unit.MILLIBAR.getFactor(), 1e-9);
        }

        @Test
        public void testETemperaturesKelvinCelsiusFahrenheitConversions() {
            // Celsius -> Kelvin
            assertEquals(273.15, Temperature.Unit.CELSIUS.toBase(0.0), 1e-9);
            // Kelvin -> Celsius
            assertEquals(0.0, Temperature.Unit.CELSIUS.fromBase(273.15), 1e-9);

            // Fahrenheit -> Kelvin (32°F = 273.15 K)
            assertEquals(273.15, Temperature.Unit.FAHRENHEIT.toBase(32.0), 1e-9);
            // Kelvin -> Fahrenheit (273.15 K = 32°F)
            assertEquals(32.0, Temperature.Unit.FAHRENHEIT.fromBase(273.15), 1e-9);

            // Round-trip arbitrary value
            double c = 37.0;
            double k = Temperature.Unit.CELSIUS.toBase(c);
            double f = Temperature.Unit.FAHRENHEIT.fromBase(k);
            double backK = Temperature.Unit.FAHRENHEIT.toBase(f);
            double backC = Temperature.Unit.CELSIUS.fromBase(backK);
            assertEquals(c, backC, 1e-9);
        }

        @Test
        public void testGetDimensionForEachUnitEnum() {
            assertEquals(EDimensions.LENGTH, Length.Unit.METER.getDimension());
            assertEquals(EDimensions.ANGLE, Angle.Unit.RADIAN.getDimension());
            assertEquals(EDimensions.TIME, Time.Unit.SECOND.getDimension());
            assertEquals(EDimensions.SPEED, Speed.Unit.METER_HR.getDimension());
            assertEquals(EDimensions.AREA, Area.Unit.SQ_METER.getDimension());
            assertEquals(EDimensions.VOLUME, Volume.Unit.CU_METER.getDimension());
            assertEquals(EDimensions.PRESSURE, Pressure.Unit.PASCAL.getDimension());
            assertEquals(EDimensions.TEMPERATURE, Temperature.Unit.KELVIN.getDimension());
        }

        @Test
        public void testGetBaseUnitFromIUnitUsesDimension() {
            assertEquals(Length.Unit.METER, Length.Unit.KILOMETER.getBaseUnit());
            assertEquals(Angle.Unit.RADIAN, Angle.Unit.DEGREE.getBaseUnit());
            assertEquals(Time.Unit.SECOND, Time.Unit.HOUR.getBaseUnit());
            assertEquals(Speed.Unit.METER_HR, Speed.Unit.MILE_HR.getBaseUnit());
            assertEquals(Area.Unit.SQ_METER, Area.Unit.SQ_KILOMETER.getBaseUnit());
            assertEquals(Volume.Unit.CU_METER, Volume.Unit.CU_MILE.getBaseUnit());
            assertEquals(Pressure.Unit.PASCAL, Pressure.Unit.BAR.getBaseUnit());
            assertEquals(Temperature.Unit.KELVIN, Temperature.Unit.CELSIUS.getBaseUnit());
        }
    }
}