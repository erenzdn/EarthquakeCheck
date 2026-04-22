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
    public LocationDTO getCoordinates(@RequestParam String address){
        double[] coordinates=geoLocationService.getCoordinatesFromAddress(address);
        if (coordinates != null) {
            return new LocationDTO(coordinates[0], coordinates[1]);
        } else {
            return new LocationDTO(0, 0);
        }
    }
}
