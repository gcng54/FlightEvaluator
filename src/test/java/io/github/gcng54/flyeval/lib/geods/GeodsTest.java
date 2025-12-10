package io.github.gcng54.flyeval.lib.geods;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Nested;

import io.github.gcng54.flyeval.lib.units.*;


class GeodsTest {

    @Nested
    public class EGeodeticsTest {        
        @Test
        void testCartesianConstructorAndGetters() { // NOSONAR
                Length x = Length.fromLength(1.0, Length.Unit.METER);
                Length y = Length.fromLength(2.0, Length.Unit.METER);
                Length z = Length.fromLength(3.0, Length.Unit.METER);
                Cartesian c = new Cartesian(x, y, z);

                assertEquals(x, c.getX());
                assertEquals(y, c.getY());
                assertEquals(z, c.getZ());
        }

        @Test
        void testCartesianZero() {
                Cartesian zero = new Cartesian();
                assertEquals(0.0, zero.getX().getBase(), 1e-10);
                assertEquals(0.0, zero.getY().getBase(), 1e-10);
                assertEquals(0.0, zero.getZ().getBase(), 1e-10);
        }

        @Test
        void testCartesianAddSub() { // NOSONAR
                Cartesian a = new Cartesian(Length.fromLength(1, Length.Unit.METER),
                                Length.fromLength(2, Length.Unit.METER),
                                Length.fromLength(3, Length.Unit.METER));
                Cartesian b = new Cartesian(Length.fromLength(4, Length.Unit.METER),
                                Length.fromLength(5, Length.Unit.METER),
                                Length.fromLength(6, Length.Unit.METER));
                Cartesian sum = a.add(b);
                Cartesian diff = b.subtract(a);

                assertEquals(5.0, sum.getX().getBase(), 1e-10);
                assertEquals(7.0, sum.getY().getBase(), 1e-10);
                assertEquals(9.0, sum.getZ().getBase(), 1e-10);

                assertEquals(3.0, diff.getX().getBase(), 1e-10);
                assertEquals(3.0, diff.getY().getBase(), 1e-10);
                assertEquals(3.0, diff.getZ().getBase(), 1e-10);
        }

        @Test
        void testCartesianMagnitudeAndNormalize() { // NOSONAR
                Cartesian c = new Cartesian(Length.fromLength(3, Length.Unit.METER),
                                Length.fromLength(4, Length.Unit.METER),
                                Length.fromLength(0, Length.Unit.METER));
                assertEquals(5.0, c.magnitude(), 1e-10);

                Cartesian norm = c.normalize();
                assertEquals(1.0, norm.magnitude(), 1e-10);
        }

        @Test
        void testCartesianDotAndCross() { // NOSONAR
                Cartesian a = new Cartesian(Length.fromLength(1, Length.Unit.METER),
                                Length.fromLength(0, Length.Unit.METER),
                                Length.fromLength(0, Length.Unit.METER));
                Cartesian b = new Cartesian(Length.fromLength(0, Length.Unit.METER),
                                Length.fromLength(1, Length.Unit.METER),
                                Length.fromLength(0, Length.Unit.METER));
                assertEquals(0.0, a.dot(b), 1e-10);

                Cartesian cross = a.cross(b);
                assertEquals(0.0, cross.getX().getBase(), 1e-10); // NOSONAR
                assertEquals(0.0, cross.getY().getBase(), 1e-10); // NOSONAR
                assertEquals(1.0, cross.getZ().getBase(), 1e-10); // NOSONAR
        }

        @Test
        void testCartesianToSphericalAndBack() { // NOSONAR
                Cartesian c = new Cartesian(Length.fromLength(1, Length.Unit.METER),
                                Length.fromLength(1, Length.Unit.METER),
                                Length.fromLength(1, Length.Unit.METER));
                Spherical s = c.toSpherical();
                Length r = Length.fromLength(c.magnitude(), Length.Unit.METER);
                Cartesian c2 = (Cartesian) s.toCartesian(r);
                assertEquals(c.getX().getBase(), c2.getX().getBase(), 1e-10);
                assertEquals(c.getY().getBase(), c2.getY().getBase(), 1e-10);
                assertEquals(c.getZ().getBase(), c2.getZ().getBase(), 1e-10);
        }

        @Test
        void testSphericalConstructorAndGetters() { // NOSONAR
                Angle az = Angle.fromAzimuthDeg(45.0);
                Angle el = Angle.fromElevationDeg(30.0);
                Length r = Length.fromLength(10.0, Length.Unit.METER);
                Spherical s = new Spherical(az, el, r);

                assertEquals(az, s.getAzimuth());
                assertEquals(el, s.getElevation());
                assertEquals(r, s.getRange());
        }

        @Test
        void testSphericalZero() {
                Spherical zero = Spherical.ZERO;
                assertEquals(0.0, zero.getAzimuth().getValue(), 1e-10);
                assertEquals(0.0, zero.getElevation().getValue(), 1e-10);
                assertEquals(0.0, zero.getRange().getValue(), 1e-10);
        }

        @Test
        void testCartesianClamp() { // NOSONAR
                Cartesian c = new Cartesian(Length.fromMeter(5), Length.fromMeter(10),
                                Length.fromMeter(15));
                Cartesian min = new Cartesian(Length.fromMeter(0), Length.fromMeter(8),
                                Length.fromMeter(12));
                Cartesian max = new Cartesian(Length.fromMeter(6), Length.fromMeter(12),
                                Length.fromMeter(20));
                Cartesian clamped = c.clamp(min, max, Utils.EWrapMode.BOUND);

                assertEquals(5.0, clamped.getX().getBase(), 1e-10); // in range
                assertEquals(10.0, clamped.getY().getBase(), 1e-10); // in range
                assertEquals(15.0, clamped.getZ().getBase(), 1e-10); // in range
        }

        @Test
        void testCartesianEqualsAndIsZero() { // NOSONAR
                Cartesian c1 = new Cartesian(Length.fromMeter(0),
                                Length.fromMeter(0), Length.fromMeter(0));
                Cartesian c2 = new Cartesian(Length.fromMeter(1e-11),
                                Length.fromMeter(-1e-11), Length.fromMeter(1e-11));
                assertTrue(c1.isZero(1e-10));
                assertTrue(c1.equals(c2, 1e-10));
        }

        @Test
        void testCartesianExceptions() { // NOSONAR
                assertThrows(IllegalArgumentException.class,
                                () -> new Cartesian(null, Length.fromMeter(1),
                                                Length.fromMeter(1)));
                assertThrows(IllegalArgumentException.class,
                                () -> new Cartesian(Length.fromMeter(1), null,
                                                Length.fromMeter(1)));
                assertThrows(IllegalArgumentException.class,
                                () -> new Cartesian(Length.fromMeter(1), Length.fromMeter(1),
                                                null));

                Cartesian zero = new Cartesian();
                assertThrows(IllegalArgumentException.class, zero::normalize);
                assertThrows(IllegalArgumentException.class, () -> zero.invert());
                assertThrows(IllegalArgumentException.class, () -> zero.divide(0.0));
                assertThrows(IllegalArgumentException.class, () -> zero.ratio(zero));
        }
    }

    @Nested
    public class GeodetsGisTest {

        @Test
        public void testGeodeticConstructorAndZero() {
                Geodetic zero = new Geodetic(0.0, 0.0, 0.0);
                assertNotNull(zero);
                assertEquals(0.0, zero.lon().getBase(), 1e-9);
                assertEquals(0.0, zero.lat().getBase(), 1e-9);
                assertEquals(0.0, zero.alt().getBase(), 1e-9);
        }

        @Test
        public void testGeodeticCustomConstructor() {
                Geodetic g = new Geodetic(10.0, 20.0, 100.0);
                assertEquals(10.0, g.lon().inDegrees(), 1e-9);
                assertEquals(20.0, g.lat().inDegrees(), 1e-9);
                assertEquals(100.0, g.alt().getBase(), 1e-9);
        }

        @Test
        public void testGeodeticNullComponents() {
                assertThrows(IllegalArgumentException.class, () -> {
                new Geodetic(null, null, null);
                });
        }

        @Test
        public void testGeodeticToGeocentricAndBack() {
                Geodetic g = new Geodetic(10.0, 20.0, 100.0);
                Geocentric ecef = g.toGeocentric();
                assertNotNull(ecef);
                Geodetic g2 = ecef.toGeodetic();
                assertNotNull(g2);
                // Should be close to original values
                assertEquals(g.lat().inDegrees(), g2.lat().inDegrees(), 1e-6);
                assertEquals(g.lon().inDegrees(), g2.lon().inDegrees(), 1e-6);
                assertEquals(g.alt().getBase(), g2.alt().getBase(), 1e-3);
        }

        @Test
        public void testGeodeticDistance() {
                Geodetic g1 = new Geodetic(0.0, 0.0, 0.0);
                Geodetic g2 = new Geodetic(0.0, 1.0, 0.0);
                Length dist = g1.getDistance(g2);
                assertTrue(dist.getBase() > 0);
        }

        @Test
        public void testGeodeticSurfaceDistance() {
                Geodetic g1 = new Geodetic(0.0, 0.0, 0.0);
                Geodetic g2 = new Geodetic(0.0, 1.0, 0.0);
                Length surfaceDist = g1.getDistanceSurface(g2);
                assertTrue(surfaceDist.getBase() > 0);
        }

        @Test
        public void testGeodeticAzimuth() {
                Geodetic g1 = new Geodetic(0.0, 0.0, 0.0);
                Geodetic g2 = new Geodetic(1.0, 1.0, 0.0);
                Angle az = g1.getAzimuth(g2);
                assertNotNull(az);
        }

        @Test
        public void testGeocentricConstructorAndZero() {
                Geocentric zero = Geocentric.ZERO;
                assertNotNull(zero);
                assertEquals(0.0, zero.getX().getBase(), 1e-9);
                assertEquals(0.0, zero.getY().getBase(), 1e-9);
                assertEquals(0.0, zero.getZ().getBase(), 1e-9);
        }

        @Test
        public void testGeocentricNullComponents() {
                assertThrows(IllegalArgumentException.class, () -> {
                new Geocentric(null, null, null);
                });
        }

        @Test
        public void testGeodeticToString() {
                Geodetic g = new Geodetic(12.345, 67.89, 123.0);
                String str = g.toString();
                assertNotNull(str);
                assertTrue(str.contains("67"));
                assertTrue(str.contains("12"));
                assertTrue(str.contains("123"));
        }

        @Test
        public void testGeocentricToGeodeticZero() {
                Geocentric zero = Geocentric.ZERO;
                Geodetic g = zero.toGeodetic();
                assertNotNull(g);
                assertEquals(0.0, g.lon().getBase(), 1e-9);
                assertEquals(Double.NaN, g.lat().getBase(), 1e-9);
                assertEquals(Double.NaN, g.alt().getBase(), 1e-9); // 
        }

        @Test
        public void testGeodeticGetAltitudeDifference() {
                Geodetic g1 = new Geodetic(0.0, 0.0, 100.0);
                Geodetic g2 = new Geodetic(0.0, 0.0, 200.0);
                Length diff = g1.getAltitudeDifference(g2);
                assertEquals(100.0, diff.getBase(), 1e-9);
        }

        @Test
        public void testGeodeticGetElevation() {
                Geodetic g1 = new Geodetic(0.0, 0.0, 100.0);
                Geodetic g2 = new Geodetic(0.0, 5.0, 5000.0);
                Angle elev = g1.getElevation(g2);
                assertNotNull(elev);
                assertTrue(elev.getBase() > 0.0);
        }

        @Test
        public void testGeodeticTransform() {
                Geodetic g = new Geodetic(10.0, 20.0, 100.0); // NOSONAR
                Cartesian displacement = new Cartesian(
                        Length.fromLength(10.0, Length.Unit.METER),
                        Length.fromLength(20.0, Length.Unit.METER),
                        Length.fromLength(30.0, Length.Unit.METER));
                Geodetic result = g.transform(displacement);
                assertNotNull(result);
        }

        @Test
        public void testGeodeticGetCartesian() {
                Geodetic g1 = new Geodetic(0.0, 0.0, 0.0);
                Geodetic g2 = new Geodetic(1.0, 1.0, 1.0);
                Cartesian cart = g1.getCartesian(g2);
                assertNotNull(cart);
        }

        @Test
        public void testGeodeticGetSpherical() {
                Geodetic g1 = new Geodetic(0.0, 0.0, 0.0);
                Geodetic g2 = new Geodetic(1.0, 1.0, 1.0);
                Spherical sph = g1.getSpherical(g2);
                assertNotNull(sph);
        }

        @Test
        public void testGeodeticToENU() {
                Geodetic g = new Geodetic(10.0, 20.0, 100.0);
                Cartesian ecefVector = new Cartesian(
                        Length.fromLength(1.0, Length.Unit.METER),
                        Length.fromLength(2.0, Length.Unit.METER),
                        Length.fromLength(3.0, Length.Unit.METER));
                Cartesian enu = g.toENU(ecefVector);
                assertNotNull(enu);
        }

        @Test
        public void testGeodeticGetAzimuthSpherical() {
                Geodetic g1 = new Geodetic(0.0, 0.0, 0.0);
                Geodetic g2 = new Geodetic(90.0, 0.0, 0.0);
                Angle az = g1.getAzimuth(g2);
                assertNotNull(az);
                double azDeg = az.inDegrees();
                assertTrue(azDeg >= 0.0 && azDeg <= 360.0);
        }

        @Test
        public void testGeodeticGetDistanceSurfaceHaversine() {
                Geodetic g1 = new Geodetic(0.0, 0.0, 0.0);
                Geodetic g2 = new Geodetic(0.0, 90.0, 0.0);
                Length dist = g1.getDistanceSurface(g2);
                assertNotNull(dist);
                assertTrue(dist.getBase() > 0);
        }

        @Test
        public void testGeocentricToString() {
                Geocentric ecef = new Geocentric(
                Length.fromMeter(1.0), // NOSONAR
                Length.fromMeter(2.0),
                Length.fromMeter(3.0)
                );
                String str = ecef.toString();
                assertNotNull(str);
                assertTrue(str.contains("1"));
                assertTrue(str.contains("2"));
                assertTrue(str.contains("3"));
        }

        @Test
        public void testEarth84CalculateDestinationVincenty() {
                Geodetic start = new Geodetic(0.0, 0.0, 0.0); // Equator, Prime Meridian
                Angle bearing = Angle.fromAzimuthDeg(90.0); // East
                Length distance = Length.fromDistance(111_319.491, Length.Unit.METER); // Approx 1 degree of longitude at equator

                LatLon destination = Earth84.INSTANCE.calculateDestination(start, bearing, distance);

                assertEquals(0.0, destination.lat().inDegrees(), 1e-6); // Should stay on equator
                assertEquals(1.0, destination.lon().inDegrees(), 1e-6); // Should move 1 degree East

                // Test a different bearing and distance
                start = new Geodetic(0.0, 0.0, 0.0);
                bearing = Angle.fromAzimuthDeg(0.0); // North
                distance = Length.fromDistance(110_574.348, Length.Unit.METER); // Approx 1 degree of latitude

                destination = Earth84.INSTANCE.calculateDestination(start, bearing, distance);

                assertEquals(1.0, destination.lat().inDegrees(), 1e-6); // Should move 1 degree North
                assertEquals(0.0, destination.lon().inDegrees(), 1e-6); // Should stay on prime meridian
        }

        @Test
        public void testEarthSphereCalculateDestination() {
                Geodetic start = new Geodetic(0.0, 0.0, 0.0); // Equator, Prime Meridian
                Angle bearing = Angle.fromAzimuthDeg(90.0); // East
                Length distance = Length.fromDistance(111_194.9266, Length.Unit.METER); // Approx 1 degree of longitude on WGS84 mean sphere

                LatLon destination = EarthSph.INSTANCE.calculateDestination(start, bearing, distance);

                assertEquals(0.0, destination.lat().inDegrees(), 1e-6); // Should stay on equator
                assertEquals(1.0, destination.lon().inDegrees(), 1e-3); // Should move 1 degree East

                // Test a different bearing and distance
                start = new Geodetic(0.0, 0.0, 0.0);
                bearing = Angle.fromAzimuthDeg(0.0); // North
                distance = Length.fromDistance(111_194.9266, Length.Unit.METER); // Approx 1 degree of latitude on WGS84 mean sphere
                destination = EarthSph.INSTANCE.calculateDestination(start, bearing, distance);
                assertEquals(1.0, destination.lat().inDegrees(), 1e-3); // Should move 1 degree North
                assertEquals(0.0, destination.lon().inDegrees(), 1e-6); // Should stay on prime meridian
        }
    }    
}