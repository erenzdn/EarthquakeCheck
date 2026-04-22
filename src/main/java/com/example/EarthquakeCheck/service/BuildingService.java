package com.example.EarthquakeCheck.service;

import com.example.EarthquakeCheck.DTO.BuildingRequest;
import com.example.EarthquakeCheck.DTO.EvaluationResultDTO;
import java.util.List;

public interface BuildingService {
    EvaluationResultDTO evaluateBuilding(BuildingRequest request);
    List<EvaluationResultDTO> getAllBuildings();
    EvaluationResultDTO getBuildingById(Long id);
    EvaluationResultDTO saveBuilding(BuildingRequest request);
    void deleteBuilding(Long id);
}
