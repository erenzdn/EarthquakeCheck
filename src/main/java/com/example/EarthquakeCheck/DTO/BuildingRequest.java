package com.example.EarthquakeCheck.DTO;

import lombok.Data;

@Data
public class BuildingRequest {
    private int buildingAge;
    private int floorCount;
    private LocationDTO location;

    public BuildingRequest() {
    }

    public BuildingRequest(int buildingAge, int floorCount, LocationDTO location) {
        this.buildingAge = buildingAge;
        this.floorCount = floorCount;
        this.location = location;
    }

} 