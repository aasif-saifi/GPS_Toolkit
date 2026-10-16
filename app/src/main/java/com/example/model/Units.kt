package com.example.model

enum class SpeedUnit(val label: String, val shortLabel: String) {
    KMH("Kilometers per hour", "km/h"),
    MPH("Miles per hour", "mph"),
    KNOTS("Knots", "kn"),
    MS("Meters per second", "m/s")
}

enum class AltitudeUnit(val label: String, val shortLabel: String) {
    METERS("Meters", "m"),
    FEET("Feet", "ft")
}

enum class AppThemeMode(val label: String) {
    DARK("Tactical Dark"),
    LIGHT("Instrument Light"),
    SYSTEM("System Default")
}

enum class NorthReference(val label: String) {
    MAGNETIC("Magnetic North"),
    TRUE("True North")
}
