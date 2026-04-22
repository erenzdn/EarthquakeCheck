package com.example.EarthquakeCheck.service;

import com.example.EarthquakeCheck.DTO.BuildingRequest;
import com.example.EarthquakeCheck.DTO.EvaluationResponseDTO;

public interface EvaluationService {
    EvaluationResponseDTO evaluateBuilding(BuildingRequest request);
} 