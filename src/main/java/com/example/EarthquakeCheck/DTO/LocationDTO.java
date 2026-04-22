package com.example.EarthquakeCheck.DTO;

import lombok.Data;

@Data
public class LocationDTO {
    private double latitude;
    private double longitude;
    
    public LocationDTO(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
}
