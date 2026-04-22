package com.example.EarthquakeCheck.service;

import java.util.Optional;

public interface GeocodingService {
    Optional<CoordinatePair> getCoordinatesFromAddress(String address);

    record CoordinatePair(double latitude, double longitude) {
    }
}
