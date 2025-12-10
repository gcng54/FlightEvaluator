package io.github.gcng54.flyeval.lib.units;

/** Cardinal and inter-cardinal compass directions. */
public enum EOrientation {

    /** Direction pointing toward geographic north (0°). */
    NORTH("N", 0.0),
    /** Direction pointing toward geographic south (180°). */
    SOUTH("S", 180.0),
    /** Direction pointing toward geographic east (90°). */
    EAST("E", 90.0),
    /** Direction pointing toward geographic west (270°). */
    WEST("W", 270.0),

    /** Direction midway between north and east (45°). */
    NORTHEAST("NE", 45.0),
    /** Direction midway between north and west (315°). */
    NORTHWEST("NW", 315.0),
    /** Direction midway between south and east (135°). */
    SOUTHEAST("SE", 135.0),
    /** Direction midway between south and west (225°). */
    SOUTHWEST("SW", 225.0);

    private final String symbol;
    private final double degrees;

    EOrientation(String symbol, double degrees) {
        this.symbol = symbol;
        this.degrees = degrees;
    }

    /**
     * Converts an orientation to degrees from north, clockwise.
     *
     * @param orientation direction enum
     * @return degrees in [0, 360)
     */
    public static double toDegree(EOrientation orientation) {
        return switch (orientation) {
            case NORTH -> 0.0;
            case NORTHEAST -> 45.0;
            case EAST -> 90.0;
            case SOUTHEAST -> 135.0;
            case SOUTH -> 180.0;
            case SOUTHWEST -> 225.0;
            case WEST -> 270.0;
            case NORTHWEST -> 315.0;
        };
    }

    /**
     * Maps a degree value to the nearest cardinal direction (N, E, S, W).
     *
     * @param degree input degrees, any real number
     * @return nearest cardinal orientation
     */
    public static EOrientation fromDegreeBase(double degree) {
        degree = ((degree % 360) + 360) % 360; // Normalize to [0, 360)
        if (degree >= 315 || degree < 45) {
            return EOrientation.NORTH;
        } else if (degree >= 45 && degree < 135) {
            return EOrientation.EAST;
        } else if (degree >= 135 && degree < 225) {
            return EOrientation.SOUTH;
        } else {
            return EOrientation.WEST;
        }
    }

    /**
     * Maps a degree value to the nearest 8-point compass orientation.
     *
     * @param degree input degrees, any real number
     * @return nearest orientation in 45° increments
     */
    public static EOrientation fromDegree(double degree) {
        degree = ((degree % 360) + 360) % 360; // Normalize to [0, 360)
        if (degree >= 337.5 || degree < 22.5) {
            return EOrientation.NORTH;
        } else if (degree >= 22.5 && degree < 67.5) {
            return EOrientation.NORTHEAST;
        } else if (degree >= 67.5 && degree < 112.5) {
            return EOrientation.EAST;
        } else if (degree >= 112.5 && degree < 157.5) {
            return EOrientation.SOUTHEAST;
        } else if (degree >= 157.5 && degree < 202.5) {
            return EOrientation.SOUTH;
        } else if (degree >= 202.5 && degree < 247.5) {
            return EOrientation.SOUTHWEST;
        } else if (degree >= 247.5 && degree < 292.5) {
            return EOrientation.WEST;
        } else {
            return EOrientation.NORTHWEST;
        }
    }

    /**
     * @return short textual symbol for the orientation (e.g., "N").
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @return orientation expressed in degrees from north.
     */
    public double getDegree() {
        return degrees;
    }

}

