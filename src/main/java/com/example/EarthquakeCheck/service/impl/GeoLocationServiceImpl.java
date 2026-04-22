package com.example.EarthquakeCheck.service.impl;

import com.example.EarthquakeCheck.service.GeoLocationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeoLocationServiceImpl implements GeoLocationService {

    @Value("${google.api.key}")
    private String googleApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeoLocationServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public double[] getCoordinatesFromAddress(String address) {
        try {
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                    address.replace(" ", "+") + "&key=" + googleApiKey;

            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode location = root.path("results").get(0).path("geometry").path("location");

            double latitude = location.get("lat").asDouble();
            double longitude = location.get("lng").asDouble();

            return new double[]{latitude, longitude};
        } catch (Exception e) {
            e.printStackTrace();
            return new double[]{0.0, 0.0}; // Hata durumunda default değer
        }
    }
}
