package io.github.gcng54.flyeval.lib.geods;

import io.github.gcng54.flyeval.lib.units.*;

/**
     * Record representing a 3D Cartesian vector with X, Y, and Z components as
     * {@link Length} quantities.
     *
     * @param cX The X-component.
     * @param cY The Y-component.
     * @param cZ The Z-component.
     */
    public record Cartesian(Length cX, Length cY, Length cZ) implements ICartesian<Cartesian> {

        public Cartesian {
            if (cX == null || cY == null || cZ == null) {
                throw new IllegalArgumentException("Cartesian components cannot be null.");
            }
        }

        public Cartesian(Length x, Length y) {
            this(x, y, Length.fromMeter(0.0));
        }

        public Cartesian() {
            this(Length.fromMeter(0.0),
                    Length.fromMeter(0.0),
                    Length.fromMeter(0.0));
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
        public Cartesian create(Length x, Length y, Length z) {
            return new Cartesian(x, y, z);
        }

    }

