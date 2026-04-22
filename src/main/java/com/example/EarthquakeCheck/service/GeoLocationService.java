package com.example.EarthquakeCheck.service;

public interface GeoLocationService {
    double[] getCoordinatesFromAddress(String address);
}
