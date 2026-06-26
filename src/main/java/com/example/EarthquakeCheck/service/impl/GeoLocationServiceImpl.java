package com.example.EarthquakeCheck.service.impl;

import com.example.EarthquakeCheck.service.GeoLocationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class GeoLocationServiceImpl implements GeoLocationService {

    @Value("${google.api.key:}")
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
            if (address == null || address.isBlank()) {
                return new double[]{0.0, 0.0};
            }

            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                    address.replace(" ", "+") + "&key=" + googleApiKey;

            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isBlank()) {
                return new double[]{0.0, 0.0};
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");
            if (!results.isArray() || results.isEmpty()) {
                return new double[]{0.0, 0.0};
            }
            JsonNode location = results.get(0).path("geometry").path("location");
            if (location.isMissingNode()) {
                return new double[]{0.0, 0.0};
            }

            double latitude = location.path("lat").asDouble(0.0);
            double longitude = location.path("lng").asDouble(0.0);

            return new double[]{latitude, longitude};
        } catch (Exception e) {
            log.warn("Geolocation resolve failed for address={}", address, e);
            return new double[]{0.0, 0.0};
        }
    }
}
