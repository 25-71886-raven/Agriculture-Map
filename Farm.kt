package com.example.myapplication.model

import org.osmdroid.util.GeoPoint
import java.text.NumberFormat
import java.util.Locale

class InvalidCoordinateException(message: String) : IllegalArgumentException(message)

class CoordinateValidator {
    fun isValidLatitude(latitude: Double): Boolean = latitude in -90.0..90.0
    fun isValidLongitude(longitude: Double): Boolean = longitude in -180.0..180.0

    fun validate(location: Location) {
        if (!isValidLatitude(location.latitude)) {
            throw InvalidCoordinateException("Invalid latitude value: ${location.latitude}")
        }
        if (!isValidLongitude(location.longitude)) {
            throw InvalidCoordinateException("Invalid longitude value: ${location.longitude}")
        }
    }
}

data class Location(
    val latitude: Double,
    val longitude: Double
) {
    fun formattedCoordinates(): String {
        val latStr = String.format(Locale.US, "%.5f", latitude)
        val lngStr = String.format(Locale.US, "%.5f", longitude)
        return "$latStr° N, $lngStr° E"
    }

    fun toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)
}

abstract class CommunityFacility(
    val name: String,
    val location: Location
) {
    abstract fun displayFacilityDetails(): String
}

data class Farm(
    val name: String,
    val crop: String,
    val squareMeters: Long,
    val lat: Double,
    val lng: Double,
    val mergedParcels: Int = 1
) {
    val location: Location = Location(lat, lng)

    fun hasValidCoordinates(validator: CoordinateValidator = CoordinateValidator()): Boolean {
        return try {
            validator.validate(location)
            true
        } catch (e: InvalidCoordinateException) {
            false
        }
    }

    fun shortAreaLabel(): String {
        val hectares = squareMeters / 10_000.0
        val formattedSqm = NumberFormat.getNumberInstance(Locale.US).format(squareMeters)
        return String.format(Locale.US, "%.1f ha (%s sqm)", hectares, formattedSqm)
    }

    fun formattedArea(): String {
        val formattedSqm = NumberFormat.getNumberInstance(Locale.US).format(squareMeters)
        val hectares = squareMeters / 10_000.0
        val formattedHa = String.format(Locale.US, "%.1f", hectares)
        return "$formattedHa ha ($formattedSqm sq. m)"
    }

    fun distanceFromTownKm(): String {
        val townProper = GeoPoint(14.0728, 120.6336)
        val barangayPoint = location.toGeoPoint()
        val meters = townProper.distanceToAsDouble(barangayPoint)
        return String.format(Locale.US, "%.1f km from Town Proper", meters / 1000.0)
    }

    fun formattedLat(): String = String.format(Locale.US, "%.5f", lat)
    fun formattedLng(): String = String.format(Locale.US, "%.5f", lng)

    fun displayInfo(): String {
        val parcelNote = if (mergedParcels > 1) " ($mergedParcels combined records)" else ""
        return "Brgy. $name, Nasugbu, Batangas$parcelNote\n" +
                "Crops: $crop\n" +
                "Land Area: ${formattedArea()}\n" +
                "GPS Coordinates: ${formattedLat()}° N, ${formattedLng()}° E\n" +
                "Distance: ${distanceFromTownKm()}"
    }
}