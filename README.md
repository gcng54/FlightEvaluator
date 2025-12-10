# FlightEvaluator

A comprehensive Java library and GUI application for aviation calculations, including unit conversions, geodetic computations, and radar-related calculations with atmospheric refraction modeling.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Features

### 📐 Unit System
- **Length**: meters, kilometers, feet, inches, yards, miles, nautical miles, flight levels, datamiles
- **Angle**: radians, degrees, DMS (Degrees-Minutes-Seconds), gradians, arc units
- **Speed**: various combinations (meter/hr, km/hr, knots, etc.)
- **Area**: square units for all length types
- **Volume**: cubic units for all length types
- **Pressure**: hectopascal, pascal, bar, millibar, PSI, inHg, mmHg
- **Temperature**: Celsius, Fahrenheit, Kelvin
- **Time**: seconds, minutes, hours, days

### 🌍 Geodetic Calculations
- Coordinate conversions (Geodetic ↔ Geocentric ↔ Cartesian ↔ Spherical)
- Great circle distance calculations (Haversine formula)
- Straight-line (chord) distance between geodetic points
- Azimuth and elevation angle calculations
- ENU (East-North-Up) and ECEF (Earth-Centered Earth-Fixed) frame transformations
- Support for both WGS84 ellipsoid and spherical Earth models

### 📡 Radar Calculations
- Radar detection conversions (Geodetic ↔ Spherical coordinates)
- Atmospheric refraction modeling with configurable k-factor
- Standard atmospheric refraction (k=4/3) support
- Custom atmospheric profiles with pressure, temperature, and humidity
- Radar horizon distance calculations
- Batch processing of multiple targets

### 🖥️ GUI Application
- Unit conversion calculator
- Geodetic coordinate conversion tool
- Radar coordinate conversion interface
- Interactive JavaFX-based user interface

## Requirements

- **Java**: 25 or higher
- **Maven**: 3.6 or higher
- **JavaFX**: 21.0.6 (included as dependency)

## Installation

### Clone the Repository

```bash
git clone https://github.com/gcng54/FlightEvaluator.git
cd FlightEvaluator
```

### Build with Maven

```bash
mvn clean install
```

### Run the GUI Application

```bash
mvn javafx:run
```

## Usage Examples

### 1. Unit Conversions

#### Length Conversions

```java
import io.github.gcng54.flyeval.lib.units.Length;

// Create a length in feet and convert to meters
Length altitude = Length.of(10000, Length.Unit.FOOT);
double altitudeInMeters = altitude.inMeter();  // 3048.0

// Flight level conversions
Length fl350 = Length.fromFlightLevel(350);
double fl350InFeet = fl350.inFoot();  // 35000.0

// Nautical miles to kilometers
Length distance = Length.of(100, Length.Unit.NAUTICAL);
double distanceInKm = distance.inKilometer();  // 185.2
```

#### Angle Conversions

```java
import io.github.gcng54.flyeval.lib.units.Angle;
import io.github.gcng54.flyeval.lib.units.DegMinSec;

// Degrees to radians
Angle heading = Angle.fromAzimuthDeg(45.0);
double headingRad = heading.inRadians();  // 0.7854

// DMS (Degrees-Minutes-Seconds) format
Angle latitude = Angle.fromLatitudeDeg(40.7128);
String dmsString = latitude.toDMSString();  // "40°42'46.08""

// Create from DMS string
double lonDeg = DegMinSec.parseDMS("74°00'21.6\"W");  // -74.006
Angle lon = Angle.fromLongitudeDeg(lonDeg);
```

#### Speed Conversions

```java
import io.github.gcng54.flyeval.lib.units.Speed;
import io.github.gcng54.flyeval.lib.units.Time;

// Knots to meters per second
Speed velocity = new Speed(250, Speed.Unit.NAUTICAL_HR);
double velocityMps = velocity.inUnit(Speed.Unit.METER_HR) / 3600;

// Derive speed from length and time
Length distance = Length.of(100, Length.Unit.NAUTICAL);
Time duration = new Time(1, Time.Unit.HOUR);
Speed speed = distance.divide(duration);  // 100 knots
```

### 2. Geodetic Calculations

#### Basic Geodetic Operations

```java
import io.github.gcng54.flyeval.lib.geods.Geodetic;
import io.github.gcng54.flyeval.lib.units.Angle;
import io.github.gcng54.flyeval.lib.units.Length;

// Create geodetic positions (longitude, latitude, altitude)
Geodetic newYork = new Geodetic(-74.006, 40.7128, 10.0);
Geodetic london = new Geodetic(-0.1276, 51.5074, 25.0);

// Calculate great circle distance
Length distance = newYork.getDistanceSurface(london);
double distanceNM = distance.inNautical();  // ~3000 NM

// Calculate straight-line (chord) distance
Length chordDistance = newYork.getDistance(london);

// Calculate initial bearing (azimuth)
Angle bearing = newYork.getAzimuth(london);
double bearingDeg = bearing.inDegrees();  // ~51°
```

#### Coordinate Frame Transformations

```java
import io.github.gcng54.flyeval.lib.geods.*;

// Geodetic to Geocentric (ECEF)
Geodetic point = new Geodetic(0.0, 45.0, 1000.0);
Geocentric ecef = point.toGeocentric();

// Working with Cartesian vectors
Cartesian displacement = new Cartesian(
    Length.of(1000, Length.Unit.METER),
    Length.of(2000, Length.Unit.METER),
    Length.of(500, Length.Unit.METER)
);

// Transform a point by a displacement vector
Geodetic newPoint = point.transform(displacement);

// Convert displacement to local ENU (East-North-Up) frame
Cartesian enuVector = point.toENU(displacement);
```

#### Spherical Coordinates

```java
import io.github.gcng54.flyeval.lib.geods.Spherical;

// Get spherical coordinates of one point relative to another
Geodetic observer = new Geodetic(0.0, 0.0, 0.0);
Geodetic target = new Geodetic(0.1, 0.1, 5000.0);

Spherical relative = observer.getSpherical(target);
Angle azimuth = relative.azimuth();     // ~45°
Angle elevation = relative.elevation(); // Positive angle above horizon
Length range = relative.range();        // Slant range
```

### 3. Radar Calculations

#### Basic Radar Conversions

```java
import io.github.gcng54.flyeval.lib.radars.Radiations;
import io.github.gcng54.flyeval.lib.geods.*;
import io.github.gcng54.flyeval.lib.units.*;

// Define radar position
Geodetic radarPos = new Geodetic(30.0, 40.0, 100.0);

// Define target position
Geodetic targetPos = new Geodetic(30.5, 40.5, 5000.0);

// Convert target to radar spherical coordinates (azimuth, elevation, range)
Spherical detection = Radiations.toSpherical(radarPos, targetPos);

Angle azimuth = detection.azimuth();
Angle elevation = detection.elevation();
Length slantRange = detection.range();

System.out.printf("Target at: Az=%.2f° El=%.2f° Range=%.1f NM%n",
    azimuth.inDegrees(), 
    elevation.inDegrees(), 
    slantRange.inNautical());
```

#### Convert Radar Detection to Geodetic Position

```java
// Radar observes a target
Geodetic radarPos = new Geodetic(0.0, 0.0, 100.0);

// Radar detection: azimuth 45°, elevation 5°, range 50 km
Spherical detection = new Spherical(
    Angle.fromAzimuthDeg(45.0),
    Angle.fromElevationDeg(5.0),
    Length.of(50, Length.Unit.KILOMETER)
);

// Calculate target's geodetic position
Geodetic targetPos = Radiations.toGeodetic(radarPos, detection);

System.out.printf("Target position: %.4f°, %.4f°, %.0f m%n",
    targetPos.lon().inDegrees(),
    targetPos.lat().inDegrees(),
    targetPos.alt().inMeter());
```

#### Atmospheric Refraction

```java
// Use custom k-factor for non-standard atmospheric conditions
double kFactor = 1.2;  // Less refraction than standard (4/3)

Spherical detection = Radiations.toSpherical(radarPos, targetPos, kFactor);
Geodetic calculatedPos = Radiations.toGeodetic(radarPos, detection, kFactor);

// Use detailed atmospheric profile
Pressure radarPressure = Pressure.fromHectopascal(1013.25);
Temperature radarTemp = Temperature.fromCelsius(15.0);
double radarHumidity = 50.0;  // percent

Pressure targetPressure = Pressure.fromHectopascal(800.0);
Temperature targetTemp = Temperature.fromCelsius(5.0);
double targetHumidity = 30.0;

Spherical detectionWithAtmo = Radiations.toSpherical(
    radarPos, radarPressure, radarTemp, radarHumidity,
    targetPos, targetPressure, targetTemp, targetHumidity
);
```

#### Radar Horizon

```java
// Calculate radar horizon distance
Length radarAltitude = Length.of(100, Length.Unit.METER);
Angle radarLatitude = Angle.fromLatitudeDeg(45.0);

Length horizonDistance = Radiations.getHorizonDistance(radarAltitude, radarLatitude);
double horizonNM = horizonDistance.inNautical();

System.out.printf("Radar horizon: %.1f NM%n", horizonNM);

// With custom k-factor
Length horizonCustom = Radiations.getHorizonDistance(radarAltitude, radarLatitude, 1.5);
```

#### Batch Processing

```java
import java.util.List;

// Process multiple targets at once
List<Geodetic> targets = List.of(
    new Geodetic(30.1, 40.1, 1000.0),
    new Geodetic(30.2, 40.2, 2000.0),
    new Geodetic(30.3, 40.3, 3000.0)
);

Geodetic radarPos = new Geodetic(30.0, 40.0, 100.0);

// Convert all targets to spherical coordinates
List<Spherical> detections = Radiations.toSphericals(radarPos, targets);

// Process detections
for (int i = 0; i < detections.size(); i++) {
    Spherical sph = detections.get(i);
    System.out.printf("Target %d: Az=%.1f° El=%.2f° Range=%.1f km%n",
        i + 1,
        sph.azimuth().inDegrees(),
        sph.elevation().inDegrees(),
        sph.range().inKilometer());
}
```

### 4. Working with Radar Observations

```java
import io.github.gcng54.flyeval.lib.geods.Observation;

// Create a radar observation (azimuth, range, altitude)
Observation obs = new Observation(
    Angle.fromAzimuthDeg(90.0),
    Length.of(100, Length.Unit.KILOMETER),
    Length.of(5000, Length.Unit.METER)
);

// Convert observation to spherical coordinates
Geodetic radarPos = new Geodetic(0.0, 0.0, 100.0);
Spherical sph = Radiations.toSpherical(radarPos, obs);

// Convert observation directly to geodetic position
Geodetic targetPos = Radiations.toGeodetic(radarPos, obs);

// Convert geodetic position to observation format
Observation backToObs = Radiations.toObservation(radarPos, targetPos);
```

### 5. Advanced Examples

#### Navigation Problem: Distance and Bearing

```java
// Calculate distance and bearing between two airports
Geodetic jfk = new Geodetic(-73.7781, 40.6413, 13.0);    // JFK Airport
Geodetic lhr = new Geodetic(-0.4543, 51.4700, 25.0);      // London Heathrow

// Great circle distance
Length distance = jfk.getDistanceSurface(lhr);
System.out.printf("Distance: %.0f NM%n", distance.inNautical());

// Initial heading
Angle bearing = jfk.getAzimuth(lhr);
System.out.printf("Initial bearing: %.1f°%n", bearing.inDegrees());

// Altitude difference
Length altDiff = jfk.getAltitudeDifference(lhr);
System.out.printf("Altitude difference: %.0f m%n", altDiff.inMeter());
```

#### Air Traffic Control Scenario

```java
// ATC radar tracking an aircraft
Geodetic atcRadar = new Geodetic(-77.037, 38.8521, 50.0);  // Washington DC area

// Aircraft position
Geodetic aircraft = new Geodetic(-77.1, 38.9, 3048.0);  // 10,000 ft

// Calculate radar view
Spherical radarView = Radiations.toSpherical(atcRadar, aircraft);

System.out.printf("Aircraft at:%n");
System.out.printf("  Azimuth: %.1f°%n", radarView.azimuth().inDegrees());
System.out.printf("  Elevation: %.2f°%n", radarView.elevation().inDegrees());
System.out.printf("  Range: %.1f NM%n", radarView.range().inNautical());
System.out.printf("  Altitude: %.0f ft%n", aircraft.alt().inFoot());
```

#### Multiple Earth Models

```java
import io.github.gcng54.flyeval.lib.geods.*;

// The library supports different Earth models via IEarthModel
// Default is WGS84 ellipsoid model (Earth84.INSTANCE)
// Also available: Spherical model (EarthSph.INSTANCE)

// Calculate distance using current model
Geodetic p1 = new Geodetic(0.0, 0.0, 0.0);
Geodetic p2 = new Geodetic(1.0, 1.0, 0.0);

Length surfaceDistance = p1.getDistanceSurface(p2);

// Use Haversine formula (spherical approximation)
Length haversineDistance = p1.getDistanceSurfaceHaversine(p2);

// Custom radius for specific calculations
Length customRadius = Length.of(6371, Length.Unit.KILOMETER);
Length customDistance = p1.getDistanceSurfaceHaversine(p2, customRadius);
```

## API Documentation

### Key Classes

- **`Length`** - Distance measurements with multiple units
- **`Angle`** - Angular measurements with orientation semantics
- **`Speed`**, **`Area`**, **`Volume`** - Derived quantities
- **`Geodetic`** - Represents a point on Earth (lon, lat, alt)
- **`Geocentric`** - ECEF Cartesian coordinates
- **`Cartesian`** - 3D Cartesian vector
- **`Spherical`** - Spherical coordinates (azimuth, elevation, range)
- **`Radiations`** - Radar calculation utilities
- **`Observation`** - Radar observation data (azimuth, range, altitude)

### Quantity System

All physical quantities extend `AQuantity<T, U>` and support:
- Unit conversions: `.inUnit(unit)`, `.inMeter()`, etc.
- Arithmetic: `.add()`, `.subtract()`, `.multiply()`, `.divide()`
- Comparisons: `.compareTo()`, `.equals()`
- Wrapping: `.wrapPositive()`, `.wrap180()`, `.wrap360()`

## GUI Application

The GUI provides three main tabs:

1. **Unit Conversion**: Convert between different units of length, angle, speed, pressure, temperature, etc.
2. **Geo Conversion**: Convert between different geodetic coordinate representations
3. **Radar Conversion**: Convert between radar spherical coordinates and geodetic positions

Launch the GUI with:
```bash
mvn javafx:run
```

## Building a JAR

To create an executable JAR:

```bash
mvn clean package
```

The JAR will be created in `target/flyeval-1.0-SNAPSHOT.jar`

## Testing

Run the test suite:

```bash
mvn test
```

Run specific tests:

```bash
mvn test -Dtest=GeodsTest
mvn test -Dtest=RadarsTest
mvn test -Dtest=UnitsTest
```

## Project Structure

```
FlightEvaluator/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── io/github/gcng54/flyeval/
│   │   │       ├── gui/              # JavaFX GUI application
│   │   │       └── lib/              # Core library
│   │   │           ├── geods/        # Geodetic calculations
│   │   │           ├── radars/       # Radar calculations
│   │   │           └── units/        # Unit system
│   │   └── resources/
│   │       └── io/github/gcng54/flyeval/gui/  # FXML files
│   └── test/
│       └── java/                     # Unit tests
├── pom.xml                           # Maven configuration
└── README.md                         # This file
```

## License

This project is licensed under the MIT License. See the [LICENSE](https://opensource.org/licenses/MIT) for details.

## Author

**Gokhan Cengiz** ([@gcng54](https://github.com/gcng54))

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Acknowledgments

- WGS84 ellipsoid model implementation
- Vincenty's formulae for geodetic calculations
- Standard atmospheric refraction modeling (k=4/3)

## References

- [WGS84 Coordinate System](https://en.wikipedia.org/wiki/World_Geodetic_System)
- [Vincenty's Formulae](https://en.wikipedia.org/wiki/Vincenty%27s_formulae)
- [Haversine Formula](https://en.wikipedia.org/wiki/Haversine_formula)
- [Atmospheric Refraction](https://en.wikipedia.org/wiki/Atmospheric_refraction)
