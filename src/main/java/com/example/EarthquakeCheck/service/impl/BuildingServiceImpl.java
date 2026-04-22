package com.example.EarthquakeCheck.service.impl;

import com.example.EarthquakeCheck.DTO.BuildingRequest;
import com.example.EarthquakeCheck.DTO.EvaluationResponseDTO;
import com.example.EarthquakeCheck.service.BuildingService;
import com.example.EarthquakeCheck.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BuildingServiceImpl implements BuildingService {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public EvaluationResponseDTO evaluateBuilding(BuildingRequest request) {
        return evaluationService.evaluateBuilding(request);
    }
} 