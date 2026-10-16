# GPS Toolkit — All-in-One Android Telemetry Suite

An all-in-one Android utility application combining an analog and digital speedometer, altimeter, tactical compass, spirit/bubble level, and live GPS coordinates in a high-contrast automotive & aviation tactical instrument cluster.

Built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Room**, and the **Android Sensor & Location APIs**.

---

## Features

### 1. Home Dashboard (Tactical Cockpit)
- Real-time speed gauge preview and large digital readout.
- Live altitude, heading with cardinal directions, surface level status, and GPS accuracy.
- GPS signal status pill (`LOCKED (±Xm)` / `SEARCHING`).
- Quick-access modules for all tools.
- Runtime location permission banner and sensor calibration guide.

### 2. Analog & Digital Speedometer
- **Custom-drawn Circular Gauge**: Smooth animated needle (spring-damped physics), 260° sweep, major/minor tick marks, numeric speed labels, and color-coded zones (Green, Yellow, Red).
- **Dual Display Modes**: Toggle seamlessly between Analog Gauge and Big Digital HUD mode.
- **Trip Session Computer**: Real-time elapsed time, trip distance, maximum recorded speed, and average speed with Start, Pause, Resume, and Reset controls.
- **Configurable Speed Limit Alert**: Audio/haptic alarm when exceeding user-set thresholds.
- **Speed Filter**: Jitter-free GPS speed sampling that clamps stationary drift (< 1 km/h) to zero.
- **Landscape & Tablet Mode**: Responsive side-by-side gauge and trip statistics layout.

### 3. Altimeter & Elevation
- Current altitude in Meters (m) and Feet (ft) above Mean Sea Level (MSL).
- Relative climb/descent tracker with a single-tap **SET ZERO** baseline button.
- Minimum and maximum session elevation tracking.
- Barometric pressure altitude estimation using device hardware `Sensor.TYPE_PRESSURE` with standard hypsometric formula.
- GPS vertical accuracy indicator.

### 4. Tactical Compass
- Rotating 360° compass dial with cardinal markers (N, NE, E, SE, S, SW, W, NW).
- True North vs Magnetic North mode using device coordinates and Android `GeomagneticField` declination model.
- Integrated **Center Bullseye Spirit Level** inside the compass dial for pitch & roll alignment.
- Live magnetic field strength in microteslas (µT).
- Course/bearing telemetry from GPS motion.
- Sensor accuracy indicator with interactive figure-8 calibration help dialog.

### 5. Spirit / Bubble Level
- **2D Circular Bullseye Level**: Responsive floating bubble with dual concentric reference rings and green glow when within level tolerance.
- **1D Tubular Vials**: Separate horizontal (roll) and vertical (pitch) liquid vials.
- Pitch and roll numerical angle readouts in degrees.
- **Surface Zero Calibration**: Calibrate relative angles against custom surfaces with reset option.
- **Haptic Pulse**: Gentle tactile feedback when passing through 0° level.

### 6. GPS Coordinates & Waypoint Manager
- Decimal Degrees (DD) and Degrees, Minutes, Seconds (DMS) coordinate formats.
- Instant single-tap clipboard copy buttons for DD and DMS.
- Native Android Share Sheet integration.
- Direct **Open in Maps** intent (`geo:lat,lng`).
- Reverse geocoding for street addresses and cities.
- **Local Waypoint Storage (Room Database)**: Save favorite waypoints with custom names, notes, and timestamps for 100% offline access.

### 7. Settings & Hardware Diagnostics
- Unit toggles: km/h, mph, knots; meters, feet.
- Gauge top speed selection: 120, 160, 200, 240.
- Speed limit alarm threshold selection.
- Level tolerance window: ±0.5°, ±1.0°, ±1.5°, ±2.0°.
- Haptic feedback & Keep Screen On switches.
- Real-time diagnostic audit of device sensors (GPS, Gyroscope, Accelerometer, Barometer, Magnetic Model).
- 100% private, on-device telemetry policy.

---

## Architecture

- **Pattern**: Clean MVVM (Model-View-ViewModel) + Repository/Tracker pattern.
- **Location Engine**: `GpsLocationTracker` using Google Play Services `FusedLocationProviderClient` with graceful fallback to Android `LocationManager`.
- **Sensors Engine**: `OrientationSensorTracker` using `Sensor.TYPE_ROTATION_VECTOR` with fallback to `Sensor.TYPE_ACCELEROMETER` + `Sensor.TYPE_MAGNETIC_FIELD`. Remaps display rotation (`0°`, `90°`, `180°`, `270°`) and applies low-pass damping.
- **Local Persistence**: SQLite via AndroidX Room (`SavedLocationDao` and `AppDatabase`).
- **UI**: 100% declarative Jetpack Compose with custom Canvas drawings for the speedometer and compass dials.

---

## Unit Testing

Run local JVM unit tests via Gradle:
```bash
gradle :app:testDebugUnitTest
```
Tests cover:
- Speed unit conversions (mps to km/h, mph, knots)
- Altitude conversions (meters to feet)
- Heading normalization and 8-point cardinal resolution
- DMS coordinate string conversions
- Hypsometric barometric altitude equations
- Angular circular smoothing filter across 0°/360° boundaries
- Speedometer session average calculations
