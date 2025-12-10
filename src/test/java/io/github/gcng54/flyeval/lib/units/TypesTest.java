package io.github.gcng54.flyeval.lib.units;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Nested;

public class TypesTest {

    @Nested
    public class ETypesNewTest {
        @Test
        void testTimestampWithinBounds() {
            Time t = Time.fromTimestamp(12345.67, Time.Unit.SECOND);
            assertEquals(12345.67, t.getValue());
            assertEquals(Time.Unit.SECOND, t.getUnit());
        }

        @Test
        void testTimestampBelowLowerBound() {
            Time t = Time.fromTimestamp(-100.0, Time.Unit.SECOND);
            assertEquals(0.0, t.getValue());
        }

        @Test
        void testTimestampAboveUpperBound() {
            Time t = Time.fromTimestamp(Double.POSITIVE_INFINITY, Time.Unit.SECOND);
            assertEquals(Double.POSITIVE_INFINITY, t.getValue());
        }

        @Test
        void testTimestampZero() {
            Time t = Time.fromTimestamp(0.0, Time.Unit.SECOND);
            assertEquals(0.0, t.getValue());
        }

        @Test
        void testDistanceWithinBounds() {
            Length l = Length.fromDistanceKm(1.0);
            assertEquals(1.0, l.getValue());
            assertEquals(Length.Unit.KILOMETER, l.getUnit());
        }

        @Test
        void testDistanceBelowLowerBound() {
            Length l = Length.fromDistanceKm(-10.0);
            assertEquals(0.0, l.getValue());
        }

        @Test
        void testAltitudeWithinBounds() {
            Length l = Length.fromAltitudeMeter(500.0);
            assertEquals(500.0, l.getValue());
            assertEquals(Length.Unit.METER, l.getUnit());
        }

        @Test
        void testAltitudeBelowLowerBound() {
            Length l = Length.fromAltitudeMeter(-500.0);
            assertEquals(0.0, l.getValue());
        }

        @Test
        void testVelocityWithinBounds() {
            Speed s = Speed.fromMeterPerHr(25.0);
            assertEquals(25.0, s.getValue());
            assertEquals(Speed.Unit.METER_HR, s.getUnit());
        }

        @Test
        void testVelocityBelowLowerBound() {
            Speed s = Speed.fromMeterPerHr(-10.0);
            assertEquals(0.0, s.getValue());
        }

        @Test
        void testAngleWithinBounds() {
            Angle a = Angle.fromDegree(-45.0);
            assertEquals(-45.0, a.getValue());
            assertEquals(Angle.Unit.DEGREE, a.getUnit());
        }

        @Test
        void testHeadingClamped() {
            Angle a = Angle.fromHeadingDeg(370.0);
            assertEquals(10.0, a.getValue(), 1e-9);
        }

        @Test
        void testLatitudeClamped() {
            Angle a = Angle.fromLatitudeDeg(-100.0);
            assertEquals(-80.0, a.getValue(), 1e-9);
            a = Angle.fromLatitudeDeg(100.0);
            assertEquals(80.0, a.getValue(), 1e-9);
        }

        @Test
        void testLongitudeClamped() {
            Angle a = Angle.fromLongitudeDeg(-200.0);
            assertEquals(160.0, a.getValue(), 1e-9);
            a = Angle.fromLongitudeDeg(200.0);
            assertEquals(-160.0, a.getValue(), 1e-9);
        }


        @Test
        void testAreaWithAreaUnit() {
            Area a = Area.fromSqMeter(500.0);
            assertEquals(500.0, a.getValue());
            assertEquals(Area.Unit.SQ_METER, a.getUnit());
        }
    }


    @Nested
    public class ETypesTest {
        @Test
        void testLengthConversion() {
            Length l1 = Length.fromKilometer(1.0);
            assertEquals(1000.0, l1.inMeter(), 1e-9);
            assertEquals(1.0, l1.inKilometer(), 1e-9);
            assertEquals(0.621371, l1.inMile(), 1e-5);
            assertEquals(3280.839895, l1.inFoot(), 1e-6);
            assertEquals(39370.07874, l1.inInch(), 1e-5);
            assertEquals(1093.613298, l1.inYard(), 1e-6);
            assertEquals(100000.0, l1.inCentimeter(), 1e-9);
            assertEquals(0.539957, l1.inNautical(), 1e-6);
        }

        @Test
        void testTimeConversion() {
            Time t1 = Time.fromHour(2.0);
            assertEquals(7200.0, t1.inUnit(Time.Unit.SECOND), 1e-9);
            assertEquals(2.0, t1.inUnit(Time.Unit.HOUR), 1e-9);
            assertEquals(120.0, t1.inUnit(Time.Unit.MINUTE), 1e-9);
        }

        @Test
        void testSpeedConversion() {
            Speed s1 = Speed.fromKilometerPerHr(36.0);
            assertEquals(36000, s1.inUnit(Speed.Unit.METER_HR), 1e-4);
            assertEquals(36.0, s1.inUnit(Speed.Unit.KILOMETER_HR), 1e-9);
            assertEquals(22.3694, s1.inUnit(Speed.Unit.MILE_HR), 1e-4);
        }

        @Test
        void testLengthDivideTimeGivesSpeed() {
            Length l = Length.fromKilometer(1000.0);
            Time t = Time.fromHour(100.0);
            Speed s = l.divide(t);
            assertEquals(10.0, s.inUnit(Speed.Unit.KILOMETER_HR), 1e-9);
        }

        @Test
        void testSpeedMultiplyTimeGivesLength() {
            Speed s = Speed.fromKilometerPerHr(10.0);
            Time t = Time.fromHour(100.0);
            Length l = s.multiply(t);
            assertEquals(1000.0, l.inKilometer(), 1e-9);
        }

        @Test
        void testAngleConversion() {
            Angle a = Angle.fromDegree(180.0);
            assertEquals(180.0, a.inDegrees(), 1e-9);
            assertEquals(Math.PI, a.inRadians(), 1e-9);
        }

        @Test
        void testAreaConversionAndOperations() {
            Length l1 = Length.fromMeter(10.0);
            Length l2 = Length.fromMeter(5.0);
            Area area = l1.multiply(l2);
            assertEquals(50.0, area.inUnit(Area.Unit.SQ_METER), 1e-9);
            Length l3 = Length.fromMeter(2.0);
            Length divided = area.divide(l3);
            assertEquals(25.0, divided.inMeter(), 1e-9);
        }

        @Test
        void testVolumeConversionAndOperations() {
            Area area = Area.fromSqMeter(20.0);
            Length length = Length.fromMeter(3.0);
            Volume volume = area.multiply(length);
            assertEquals(60.0, volume.inUnit(Volume.Unit.CU_METER), 1e-9);
        }

        @Test
        void testPressureConversion() {
            Pressure p = Pressure.fromBar(1.0);
            assertEquals(100000.0, p.inUnit(Pressure.Unit.PASCAL), 1e-6);
            assertEquals(14.5038, p.inUnit(Pressure.Unit.PSI), 1e-4);
        }

        @Test
        void testTemperatureConversion() {
            Temperature tC = Temperature.fromCelsius(0.0);
            
            Temperature tF = Temperature.fromFahrenheit(32.0);
            Temperature tK = Temperature.fromKelvin(273.15);

            assertEquals(273.15, tC.inUnit(Temperature.Unit.KELVIN), 1e-9);
            assertEquals(0.0, tK.inUnit(Temperature.Unit.CELSIUS), 1e-9);
            assertEquals(32.0, tC.inUnit(Temperature.Unit.FAHRENHEIT), 1e-9);
            assertEquals(0.0, tF.inUnit(Temperature.Unit.CELSIUS), 1e-9);
        }

        @Test
        void testAngleTrigonometricFunctions() {
            Angle a = Angle.fromAngle(90.0, Angle.Unit.DEGREE);
            assertEquals(1.0, a.sin(), 1e-9);
            assertEquals(0.0, a.cos(), 1e-9);
            assertEquals(Math.tan(Math.toRadians(90)), a.tan(), 1e6); // tan(90°) is infinite
        }
    }

}

    
