package com.example.EarthquakeCheck.service;

import com.example.EarthquakeCheck.DTO.BuildingRequestDTO;
import com.example.EarthquakeCheck.model.Building;

public interface BuildingManagementService {
    Building createBuilding(BuildingRequestDTO requestDTO);
    Building updateBuilding(Long id, BuildingRequestDTO requestDTO);
}
