package io.github.gcng54.flyeval.lib.geods;

import io.github.gcng54.flyeval.lib.units.*;

/**
     * Represents a point in a 3D Geocentric Earth-Centered, Earth-Fixed (ECEF)
     * Cartesian coordinate system.
     *
     * @param cX The X-coordinate, pointing from the Earth's center to the
     *           intersection of the prime meridian and the equator.
     * @param cY The Y-coordinate, pointing from the Earth's center to 90 degrees
     *           East longitude at the equator.
     * @param cZ The Z-coordinate, pointing from the Earth's center to the North
     *           Pole.
     */
    public record Geocentric(Length cX, Length cY,
            Length cZ) implements ICartesian<Geocentric> {

        /** A Geocentric point at the Earth's center (0, 0, 0). */
        public static final Geocentric ZERO = new Geocentric();

        public Geocentric {
            if (cX == null || cY == null || cZ == null) {
                throw new IllegalArgumentException("Geocentric components cannot be null.");
            }
        }

        /**
         * Creates a Geocentric point at the origin (0, 0, 0).
         */
        public Geocentric() {
            this(Length.fromRangeKm(0.0),
                    Length.fromRangeKm(0.0),
                    Length.fromRangeKm(0.0));
        }

        /**
         * Creates a new Geocentric instance from a generic ICartesian object.
         *
         * @param cartesian The cartesian object to copy coordinates from.
         */
        public Geocentric(ICartesian<?> cartesian) {
            this(cartesian.getX(), cartesian.getY(), cartesian.getZ());
        }

        @Override
        public Length getX() {
            return cX;
        }

        @Override
        public Length getY() {
            return cY;
        }

        @Override
        public Length getZ() {
            return cZ;
        }

        @Override
        public Geocentric create(Length x, Length y, Length z) {
            return new Geocentric(x, y, z);
        }

        @Override
        public Spherical toSpherical() {
            return ICartesian.super.toSpherical();
        }

        public Cartesian toCartesian() {
            return ICartesian.super.toCartesian();
        }

        /**
         * Converts Geocentric (ECEF) coordinates to Geodetic coordinates (latitude,
         * longitude, altitude).
         * This assumes a spherical Earth model.
         *
         * @return A new Geodetic object.
         */
        public Geodetic toGeodetic() {
            return IEarthModel.EARTH_MODEL.toGeodetic(this); // NOSONAR
        }
    }

