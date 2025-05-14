package com.example.EarthquakeCheck.service.impl;

import com.example.EarthquakeCheck.DTO.BuildingRequest;
import com.example.EarthquakeCheck.DTO.EvaluationResultDTO;
import com.example.EarthquakeCheck.service.BuildingService;
import com.example.EarthquakeCheck.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BuildingServiceImpl implements BuildingService {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public EvaluationResultDTO evaluateBuilding(BuildingRequest request) {
        return evaluationService.evaluateBuilding(request);
    }

    @Override
    public List<EvaluationResultDTO> getAllBuildings() {
        return evaluationService.getAllEvaluations();
    }

    @Override
    public EvaluationResultDTO getBuildingById(Long id) {
        return evaluationService.getEvaluationById(id);
    }

    @Override
    public EvaluationResultDTO saveBuilding(BuildingRequest request) {
        EvaluationResultDTO evaluation = evaluateBuilding(request);
        return evaluationService.saveEvaluation(evaluation);
    }

    @Override
    public void deleteBuilding(Long id) {
        // TODO: Implement delete functionality
    }
} 