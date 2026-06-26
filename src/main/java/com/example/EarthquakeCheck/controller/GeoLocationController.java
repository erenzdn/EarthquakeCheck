package com.example.EarthquakeCheck.controller;


import com.example.EarthquakeCheck.DTO.LocationDTO;
import com.example.EarthquakeCheck.service.GeoLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/geolocation")

public class GeoLocationController {
    

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
        double[] coordinates = geoLocationService.getCoordinatesFromAddress(address);
        if (coordinates == null || coordinates.length < 2) {
            return new LocationDTO(0, 0);
        }
        return new LocationDTO(coordinates[0], coordinates[1]);
    }
}
