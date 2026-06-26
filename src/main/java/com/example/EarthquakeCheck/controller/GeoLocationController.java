package com.example.EarthquakeCheck.controller;

import com.example.EarthquakeCheck.DTO.LocationDTO;
import com.example.EarthquakeCheck.service.GeoLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/geolocation")
public class GeoLocationController {

    private static final int MAX_ADDRESS_LENGTH = 500;

    @Autowired
    private GeoLocationService geoLocationService;

    @PostMapping("/coordinates")
    public LocationDTO getCoordinates(@RequestParam String address) {
        return resolveAddress(address);
    }

    @PostMapping(value = "/coordinates", consumes = "application/json")
    public LocationDTO getCoordinatesFromJson(@RequestBody java.util.Map<String, String> payload) {
        return resolveAddress(payload.get("address"));
    }

    private LocationDTO resolveAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "address zorunludur.");
        }
        if (address.length() > MAX_ADDRESS_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "address en fazla 500 karakter olabilir.");
        }

        double[] coordinates = geoLocationService.getCoordinatesFromAddress(address);
        if (coordinates == null || coordinates.length < 2) {
            return new LocationDTO(0, 0);
        }
        return new LocationDTO(coordinates[0], coordinates[1]);
    }
}
