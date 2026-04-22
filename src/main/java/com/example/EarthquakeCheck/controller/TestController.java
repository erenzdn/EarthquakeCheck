package com.example.EarthquakeCheck.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EarthquakeCheck.DTO.LocationDTO;
import com.example.EarthquakeCheck.service.GeoLocationService;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins="*")//frontend işlemi için
public class TestController {

    @Autowired
    private GeoLocationService geoLocationService;

    @GetMapping("/location")
    public ResponseEntity<LocationDTO> getCoordinates(@RequestParam String address) {
        try {
            double[] coordinates = geoLocationService.getCoordinatesFromAddress(address);
            LocationDTO result = new LocationDTO(coordinates[0], coordinates[1]);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new LocationDTO(0, 0));
        }
    }
}
