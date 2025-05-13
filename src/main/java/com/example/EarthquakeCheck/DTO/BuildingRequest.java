package com.example.EarthquakeCheck.DTO;

public class BuildingRequest {
    private int buildingAge;
    private int floorCount;

    public BuildingRequest() {
    }

    public BuildingRequest(int buildingAge, int floorCount) {
        this.buildingAge = buildingAge;
        this.floorCount = floorCount;
    }

    public int getBuildingAge() {
        return buildingAge;
    }

    public void setBuildingAge(int buildingAge) {
        this.buildingAge = buildingAge;
    }

    public int getFloorCount() {
        return floorCount;
    }

    public void setFloorCount(int floorCount) {
        this.floorCount = floorCount;
    }
}
