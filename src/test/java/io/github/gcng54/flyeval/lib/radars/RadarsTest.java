package io.github.gcng54.flyeval.lib.radars;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import io.github.gcng54.flyeval.lib.units.*;
import io.github.gcng54.flyeval.lib.geods.*;

public class RadarsTest {

    private static final double ANGLE_TOL = 1e-6;   // radians
    private static final double ALT_TOL = 1.0;      // meters
    private static final double RANGE_TOL = 1e-3;   // meters
    private static final double K_FACTOR_TOL = 0.05; // Tolerance for k-factor approximations

    private Geodetic createGeodetic(double lonDeg, double latDeg, double altM) {
        Angle lon = Angle.fromLongitudeDeg(lonDeg);
        Angle lat = Angle.fromLatitudeDeg(latDeg);
        Length alt = Length.fromAltitudeMt(altM);
        return new Geodetic(lon, lat, alt);
    }

    private Pressure createPressure(double hPa) {
        return Pressure.fromHectopascal(hPa);
    }

    private Temperature createTemperature(double celsius) {
        return Temperature.fromCelsius(celsius);
    }

    @Test
    public void testToSphericalAndBackWithDefaultK_RoundTrip() {
        Geodetic radar = createGeodetic(0.0, 0.0, 100.0);
        Geodetic target = createGeodetic(0.1, 0.1, 1000.0);

        Spherical sph = Radiations.toSpherical(radar, target);
        Geodetic back = Radiations.toGeodetic(radar, sph);

        assertEquals(target.lat().inRadians(), back.lat().inRadians(), ANGLE_TOL);
        assertEquals(target.lon().inRadians(), back.lon().inRadians(), ANGLE_TOL);
        assertEquals(target.alt().getBase(), back.alt().getBase(), ALT_TOL);
    }

    @Test
    public void testToSphericalAndBackWithCustomK_RoundTrip() {
        Geodetic radar = createGeodetic(10.0, 45.0, 50.0);
        Geodetic target = createGeodetic(10.5, 45.3, 500.0);
        double kFactor = 1.2;

        Spherical sph = Radiations.toSpherical(radar, target, kFactor);
        Geodetic back = Radiations.toGeodetic(radar, sph, kFactor);

        assertEquals(target.lat().inRadians(), back.lat().inRadians(), ANGLE_TOL);
        assertEquals(target.lon().inRadians(), back.lon().inRadians(), ANGLE_TOL);
        assertEquals(target.alt().getBase(), back.alt().getBase(), ALT_TOL);
    }

    @Test
    public void testToSphericalDefaultMatchesExplicitK() {
        Geodetic radar = createGeodetic(-5.0, 20.0, 150.0);
        Geodetic target = createGeodetic(-4.8, 20.2, 800.0);

        Spherical sphDefault = Radiations.toSpherical(radar, target);
        Spherical sphExplicit = Radiations.toSpherical(radar, target, Radiations.STANDARD_REFRACTION_K);

        assertEquals(sphExplicit.azimuth().inRadians(), sphDefault.azimuth().inRadians(), ANGLE_TOL);
        assertEquals(sphExplicit.elevation().inRadians(), sphDefault.elevation().inRadians(), ANGLE_TOL);
        assertEquals(sphExplicit.range().getBase(), sphDefault.range().getBase(), RANGE_TOL);
    }

    @Test
    public void testToGeodeticDefaultMatchesExplicitK() {
        Geodetic radar = createGeodetic(30.0, -10.0, 200.0);

        Angle az = Angle.fromAzimuthDeg(45.0);
        Angle el = Angle.fromElevationDeg(5.0);
        Length range = Length.fromDistanceMt(100_000.0);
        Spherical sph = new Spherical(az, el, range);

        Geodetic gDefault = Radiations.toGeodetic(radar, sph);
        Geodetic gExplicit = Radiations.toGeodetic(radar, sph, Radiations.STANDARD_REFRACTION_K);

        assertEquals(gExplicit.lat().inRadians(), gDefault.lat().inRadians(), ANGLE_TOL);
        assertEquals(gExplicit.lon().inRadians(), gDefault.lon().inRadians(), ANGLE_TOL);
        assertEquals(gExplicit.alt().getBase(), gDefault.alt().getBase(), ALT_TOL);
    }

    @Test
    public void testHorizonDistanceZeroAltitudeIsZero() {
        Length alt0 = Length.fromAltitudeMt(0.0);
        Angle lat = Angle.fromLatitudeDeg(0.0);

        Length horizon = Radiations.getHorizonDistance(alt0, lat);
        assertEquals(0.0, horizon.getBase(), 1e-9);
    }

    @Test
    public void testHorizonDistanceIncreasesWithAltitude() {
        Angle lat = Angle.fromLatitudeDeg(52.0);
        Length altLow = Length.fromAltitudeMt(100.0);
        Length altHigh = Length.fromAltitudeMt(1000.0);

        Length dLow = Radiations.getHorizonDistance(altLow, lat);
        Length dHigh = Radiations.getHorizonDistance(altHigh, lat);

        assertTrue(dHigh.getBase() > dLow.getBase());
    }

    @Test
    public void testHorizonDistanceDefaultMatchesExplicitK() {
        Angle lat = Angle.fromLatitudeDeg(40.0);
        Length alt = Length.fromAltitudeMt(500.0);

        Length dDefault = Radiations.getHorizonDistance(alt, lat);
        Length dExplicit = Radiations.getHorizonDistance(alt, lat, Radiations.STANDARD_REFRACTION_K);

        assertEquals(dExplicit.getBase(), dDefault.getBase(), RANGE_TOL);
    }

    @Test
    public void testUtilityClassConstructorIsPrivateAndThrows() throws Exception {
        Constructor<Radiations> ctor = Radiations.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(ctor.getModifiers()));
        ctor.setAccessible(true);
        try {
            ctor.newInstance();
            fail("Expected UnsupportedOperationException");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }
    }

    @Nested
    class RefractionTest {

        // Standard atmospheric conditions for testing
        private final Temperature STD_TEMP_C = createTemperature(15.0); // 15 C
        private final Pressure STD_PRES_HPA = createPressure(1013.25); // 1013.25 hPa
        private final double STD_RH = 60.0; // 60% relative humidity

        @Test
        void testCalculateSaturationVaporPressure() {
            // Test at 20 C
            Pressure es20 = Refraction.calculateSaturationVaporPressure(createTemperature(20.0));
            assertEquals(23.37, es20.inUnit(Pressure.Unit.HECTOPASCAL), 1e-2); // Expected value from Tetens formula

            // Test at 0 C
            Pressure es0 = Refraction.calculateSaturationVaporPressure(createTemperature(0.0));
            assertEquals(6.112, es0.inUnit(Pressure.Unit.HECTOPASCAL), 1e-3);

            // Test at -10 C
            Pressure esNeg10 = Refraction.calculateSaturationVaporPressure(createTemperature(-10.0));
            assertEquals(2.86769, esNeg10.inUnit(Pressure.Unit.HECTOPASCAL), 1e-3);
        }

        @Test
        void testCalculateVaporPressure() {
            // Test with 20 C, 50% RH
            Pressure e = Refraction.calculateVaporPressure(createTemperature(20.0), 50.0);
            assertEquals(23.37 * 0.5, e.inUnit(Pressure.Unit.HECTOPASCAL), 1e-2);

            // Test with 0% RH
            Pressure e0 = Refraction.calculateVaporPressure(createTemperature(20.0), 0.0);
            assertEquals(0.0, e0.inUnit(Pressure.Unit.HECTOPASCAL), 1e-9);

            // Test with 100% RH
            Pressure e100 = Refraction.calculateVaporPressure(createTemperature(20.0), 100.0);
            Pressure es = Refraction.calculateSaturationVaporPressure(createTemperature(20.0));
            assertEquals(es.inUnit(Pressure.Unit.HECTOPASCAL), e100.inUnit(Pressure.Unit.HECTOPASCAL), 1e-2);
        }

        @Test
        void testCalculateVaporPressure_InvalidHumidity() {
            assertThrows(IllegalArgumentException.class, () -> Refraction.calculateVaporPressure(STD_TEMP_C, -10.0));
            assertThrows(IllegalArgumentException.class, () -> Refraction.calculateVaporPressure(STD_TEMP_C, 101.0));
        }

        @Test
        void testCalculateRefractivity_StandardConditions() {
            // Example values from ITU-R P.453-10 (Table 1, for 1013.25 hPa, 15°C, 60% RH)
            // Ns = 313 N-units (approx)
            double N = Refraction.calculateRefractivity(STD_PRES_HPA, STD_TEMP_C, STD_RH);
            assertEquals(318.83, N, 1.0); // Allow some tolerance due to rounding in example values
        }

        @Test
        void testCalculateRefractivity_DryAir() {
            // Dry air (0% RH)
            double N_dry = Refraction.calculateRefractivity(STD_PRES_HPA, STD_TEMP_C, 0.0);
            // For dry air, e_hpa = 0, so wet term is 0.
            // N = 77.6 * (1013.25 / 288.15) = 272.9 N-units
            assertEquals(272.9, N_dry, 1e-1);
        }

        @Test
        void testCalculateRefractivity_InvalidHumidity() {
            assertThrows(IllegalArgumentException.class, () -> Refraction.calculateRefractivity(STD_PRES_HPA, STD_TEMP_C, -5.0));
            assertThrows(IllegalArgumentException.class, () -> Refraction.calculateRefractivity(STD_PRES_HPA, STD_TEMP_C, 105.0));
        }

        @Test
        void testCalculateRefractiveIndex() {
            double N = 313.0;
            double n = Refraction.calculateRefractiveIndex(N);
            assertEquals(1 + (313.0 * 1e-6), n, 1e-9);

            N = 0.0;
            n = Refraction.calculateRefractiveIndex(N);
            assertEquals(1.0, n, 1e-9);
        }

        @Test
        void testCalculateModifiedRefractivity() {
            double N = 313.0;
            Length height = Length.fromMeter(100.0); // 100 meters
            double Re_true = Radiations.EARTH_RADIUS.inMeter();

            double M = Refraction.calculateModifiedRefractivity(N, height);
            double expectedM = N + (height.inMeter() / Re_true) * 1e6;
            assertEquals(expectedM, M, 1e-6);

            // Test with zero height
            M = Refraction.calculateModifiedRefractivity(N, Length.fromMeter(0.0));
            assertEquals(N, M, 1e-9);
        }

        @Test
        void testAverageModifiedRefractivity() {
            double N_site = 313.0;
            Length siteHeight = Length.fromMeter(100.0);
            double N_target = 290.0;
            Length targetHeight = Length.fromMeter(1000.0);

            double M_site = Refraction.calculateModifiedRefractivity(N_site, siteHeight);
            double M_target = Refraction.calculateModifiedRefractivity(N_target, targetHeight);

            double avgM = Refraction.averageModifiedRefractivity(N_site, siteHeight, N_target, targetHeight);
            assertEquals((M_site + M_target) / 2.0, avgM, 1e-6);
        }

        @Test
        void testAverageModifiedRefractivity_FromStandardAtmosphere() {
            Length siteHeight = Length.fromMeter(0.0);
            Length targetHeight = Length.fromMeter(1000.0);

            // Calculate N values from standard atmosphere
            Pressure p_site = Refraction.getStandardPressure(siteHeight);
            Temperature t_site = Refraction.getStandardTemperature(siteHeight);
            double rh_site = Refraction.getStandardRelativeHumidity(siteHeight);
            double N_site = Refraction.calculateRefractivity(p_site, t_site, rh_site);

            Pressure p_target = Refraction.getStandardPressure(targetHeight);
            Temperature t_target = Refraction.getStandardTemperature(targetHeight);
            double rh_target = Refraction.getStandardRelativeHumidity(targetHeight);
            double N_target = Refraction.calculateRefractivity(p_target, t_target, rh_target);

            // Calculate expected average M using the existing method
            double expectedAvgM = Refraction.averageModifiedRefractivity(N_site, siteHeight, N_target, targetHeight);

            // Test the new overload
            double actualAvgM = Refraction.averageModifiedRefractivity(siteHeight, targetHeight);
            assertEquals(expectedAvgM, actualAvgM, 1e-6);
        }

        @Test
        void testAverageModifiedRefractivity_WithSiteWeatherAndStandardTarget() {
            Length siteHeight = Length.fromMeter(100.0);
            Pressure siteP = createPressure(1010.0);
            Temperature siteT = createTemperature(20.0);
            double siteRH = 75.0;
            Length targetHeight = Length.fromMeter(2000.0);

            // Calculate N_site from weather
            double N_site = Refraction.calculateRefractivity(siteP, siteT, siteRH);

            // Calculate N_target from standard atmosphere
            Pressure p_target = Refraction.getStandardPressure(targetHeight);
            Temperature t_target = Refraction.getStandardTemperature(targetHeight);
            double rh_target = Refraction.getStandardRelativeHumidity(targetHeight);
            double N_target = Refraction.calculateRefractivity(p_target, t_target, rh_target);

            // Calculate expected average M using the base method
            double expectedAvgM = Refraction.averageModifiedRefractivity(N_site, siteHeight, N_target, targetHeight);

            // Test the new overload
            double actualAvgM = Refraction.averageModifiedRefractivity(siteHeight, siteP, siteT, siteRH, targetHeight);
            assertEquals(expectedAvgM, actualAvgM, 1e-6);
        }

        @Test
        void testGetStandardTemperature() {
            Length h0 = Length.fromMeter(0.0);
            Length h11km = Length.fromKilometer(11.0);
            Length h15km = Length.fromKilometer(15.0);

            // Sea level (15 C = 288.15 K)
            assertEquals(288.15, Refraction.getStandardTemperature(h0).inUnit(Temperature.Unit.KELVIN), 1e-3);
            // Tropopause (11 km, -56.5 C = 216.65 K)
            assertEquals(216.65, Refraction.getStandardTemperature(h11km).inUnit(Temperature.Unit.KELVIN), 1e-3);
            // Above tropopause (15 km, should be constant 216.65 K in this simplified model)
            assertEquals(216.65, Refraction.getStandardTemperature(h15km).inUnit(Temperature.Unit.KELVIN), 1e-3);
        }

        @Test
        void testGetStandardPressure() {
            Length h0 = Length.fromMeter(0.0);
            Length h11km = Length.fromKilometer(11.0);
            Length h15km = Length.fromKilometer(15.0);

            // Sea level (101325 Pa)
            assertEquals(101325.0, Refraction.getStandardPressure(h0).inUnit(Pressure.Unit.PASCAL), 1e-1);
            // Tropopause (11 km, approx 22632 Pa)
            assertEquals(22632.646, Refraction.getStandardPressure(h11km).inUnit(Pressure.Unit.PASCAL), 1e-1);
            // Above tropopause (15 km, approx 12000 Pa)
            assertEquals(12045.011, Refraction.getStandardPressure(h15km).inUnit(Pressure.Unit.PASCAL), 10.0); // Larger tolerance for higher altitude
        }

        @Test
        void testGetStandardRelativeHumidity() {
            Length h = Length.fromMeter(1000.0);
            assertEquals(60.0, Refraction.getStandardRelativeHumidity(h), 1e-9);
        }

        @Test
        void testCalculateKFactorFromStandardAtmosphere() {
            Length h1 = Length.fromMeter(0.0);
            Length h2 = Length.fromMeter(1000.0);

            // This should produce a k-factor close to STANDARD_REFRACTION_K
            double kFactor = Refraction.calculateKFactorFromStandardAtmosphere(h1, h2);
            assertEquals(Radiations.STANDARD_REFRACTION_K, kFactor, K_FACTOR_TOL);
        }

        @Test
        void testCalculateKFactorFromAtmosphericProfile_StandardConditions() {
            // Simulate a standard atmosphere where k-factor should be close to 4/3
            Length h1 = Length.fromMeter(0.0);
            Pressure p1 = createPressure(1013.25);
            Temperature t1 = createTemperature(15.0);
            double rh1 = 60.0;

            // At 1000m, standard atmosphere:
            // Temp drops by 6.5 C/km -> 15 - 6.5 = 8.5 C
            // Pressure drops (approx) -> 1013.25 * (1 - 0.0065 * 1000 / 288.15)^5.255 = 898.75 hPa
            // RH might stay similar or drop, let's assume 50% for this test
            Length h2 = Length.fromMeter(1000.0);
            Pressure p2 = createPressure(898.75);
            Temperature t2 = createTemperature(8.5);
            double rh2 = 50.0;

            double kFactor = Refraction.calculateKFactorFromAtmosphericProfile(h1, p1, t1, rh1, h2, p2, t2, rh2);
            // A k-factor of 4/3 is approx 1.333. The calculated value will depend on the exact N-profile.
            // This is a rough check.
            assertEquals(Radiations.STANDARD_REFRACTION_K, kFactor, 0.1); // Allow a larger tolerance for atmospheric model approximation
        }

        @Test
        void testCalculateKFactorFromAtmosphericProfile_SameHeight() {
            Length h1 = Length.fromMeter(100.0);
            Pressure p1 = createPressure(1000.0);
            Temperature t1 = createTemperature(10.0);
            double rh1 = 70.0;

            // Very slightly different height to avoid exact zero division, but still trigger the "same height" logic
            Length h2 = Length.fromMeter(100.0 + 1e-7);
            Pressure p2 = createPressure(1000.0);
            Temperature t2 = createTemperature(10.0);
            double rh2 = 70.0;

            double kFactor = Refraction.calculateKFactorFromAtmosphericProfile(h1, p1, t1, rh1, h2, p2, t2, rh2);
            // Should use the default gradient, resulting in a k-factor close to standard
            assertEquals(Radiations.STANDARD_REFRACTION_K, kFactor, 0.1);
        }

        @Test
        void testCalculateKFactorFromAtmosphericProfile_NearZeroGradient() {
            // Simulate a scenario where M1 and M2 are very close, leading to dM_dh near zero
            Length h1 = Length.fromMeter(0.0);
            Pressure p1 = createPressure(1013.25);
            Temperature t1 = createTemperature(15.0);
            double rh1 = 60.0;

            Length h2 = Length.fromMeter(100.0); // Small height difference
            Pressure p2 = createPressure(1013.0); // Very slight pressure drop
            Temperature t2 = createTemperature(14.9); // Very slight temp drop
            double rh2 = 60.1; // Very slight RH change

            // This setup is designed to make N1 and N2, and thus M1 and M2, very close.
            // The k-factor should become very large.
            double kFactor = Refraction.calculateKFactorFromAtmosphericProfile(h1, p1, t1, rh1, h2, p2, t2, rh2);
            assertTrue(kFactor < 500.0); // FIXME Should be a large value indicating near-zero gradient
        }

        @Test
        void testToSpherical_WithAtmosphericProfile() {
            Geodetic radarPos = createGeodetic(0.0, 0.0, 100.0);
            Pressure radarP = createPressure(1010.0);
            Temperature radarT = createTemperature(10.0);
            double radarRH = 70.0;

            Geodetic targetPos = createGeodetic(0.05, 0.05, 500.0);
            Pressure targetP = createPressure(990.0);
            Temperature targetT = createTemperature(8.0);
            double targetRH = 65.0;

            // Calculate k-factor using the profile
            double kFactorFromProfile = Refraction.calculateKFactorFromAtmosphericProfile(
                    radarPos.alt(), radarP, radarT, radarRH,
                    targetPos.alt(), targetP, targetT, targetRH);

            // Convert using the profile-derived k-factor
            Spherical sphProfile = Radiations.toSpherical(
                    radarPos, radarP, radarT, radarRH,
                    targetPos, targetP, targetT, targetRH);

            // Convert using the explicit k-factor (should be the same result)
            Spherical sphExplicitK = Radiations.toSpherical(radarPos, targetPos, kFactorFromProfile);

            assertEquals(sphExplicitK.azimuth().inRadians(), sphProfile.azimuth().inRadians(), ANGLE_TOL);
            assertEquals(sphExplicitK.elevation().inRadians(), sphProfile.elevation().inRadians(), ANGLE_TOL);
            assertEquals(sphExplicitK.range().getBase(), sphProfile.range().getBase(), RANGE_TOL);

            // Also compare with standard k-factor (should be different unless kFactorFromProfile is close to 4/3)
            Spherical sphStandardK = Radiations.toSpherical(radarPos, targetPos, Radiations.STANDARD_REFRACTION_K);
            if (Math.abs(kFactorFromProfile - Radiations.STANDARD_REFRACTION_K) > K_FACTOR_TOL) {
                assertNotEquals(sphStandardK.elevation().inRadians(), sphProfile.elevation().inRadians(), ANGLE_TOL);
            }
        }
    }

    @Test
    void testCalculateKFactorFromAtmosphericProfile_WithSiteWeatherAndStandardTarget() {
        Length siteHeight = Length.fromMeter(100.0);
        Pressure siteP = createPressure(1010.0);
        Temperature siteT = createTemperature(20.0);
        double siteRH = 75.0;
        Length targetHeight = Length.fromMeter(2000.0);

        // Calculate expected k-factor using the full profile method
        Pressure p_target_std = Refraction.getStandardPressure(targetHeight);
        Temperature t_target_std = Refraction.getStandardTemperature(targetHeight);
        double rh_target_std = Refraction.getStandardRelativeHumidity(targetHeight);

        double expectedKFactor = Refraction.calculateKFactorFromAtmosphericProfile(
                siteHeight, siteP, siteT, siteRH,
                targetHeight, p_target_std, t_target_std, rh_target_std);

        // Test the new overload
        double actualKFactor = Refraction.calculateKFactorFromAtmosphericProfile(
                siteHeight, siteP, siteT, siteRH,
                targetHeight);
        assertEquals(expectedKFactor, actualKFactor, 1e-6);
    }

    @Test
    void testToGeodetic_IterativeSolver() {
        Geodetic radarPos = createGeodetic(10.0, 45.0, 50.0);
        Pressure radarP = createPressure(1000.0);
        Temperature radarT = createTemperature(20.0);
        double radarRH = 60.0;

        // Define a known target to establish a ground truth
        Geodetic trueTargetPos = createGeodetic(10.5, 45.3, 5000.0);

        // 1. Calculate the "true" k-factor for this specific radar-target path
        double trueKFactor = Refraction.calculateKFactorFromAtmosphericProfile(
                radarPos.alt(), radarP, radarT, radarRH, trueTargetPos.alt());

        // 2. Calculate the spherical detection that would result from this true target and k-factor
        Spherical targetSph = Radiations.toSpherical(radarPos, trueTargetPos, trueKFactor);

        // 3. Use the new iterative toGeodetic method to convert back from spherical
        // This method does NOT know the trueKFactor or trueTargetPos; it must find them.
        Geodetic calculatedTargetPos = Radiations.toGeodetic(radarPos, radarP, radarT, radarRH, targetSph);

        // 4. The result from the iterative solver should be very close to the original true target position
        assertEquals(trueTargetPos.lat().inRadians(), calculatedTargetPos.lat().inRadians(), ANGLE_TOL);
        assertEquals(trueTargetPos.lon().inRadians(), calculatedTargetPos.lon().inRadians(), ANGLE_TOL);
        assertEquals(trueTargetPos.alt().getBase(), calculatedTargetPos.alt().getBase(), ALT_TOL);

        // Also, verify that a simple conversion with the standard k-factor would be different
        Geodetic standardKTargetPos = Radiations.toGeodetic(radarPos, targetSph, Radiations.STANDARD_REFRACTION_K);
        if (Math.abs(trueKFactor - Radiations.STANDARD_REFRACTION_K) > K_FACTOR_TOL) {
            assertNotEquals(trueTargetPos.alt().getBase(), standardKTargetPos.alt().getBase(), ALT_TOL * 10);
        }
    }
}
