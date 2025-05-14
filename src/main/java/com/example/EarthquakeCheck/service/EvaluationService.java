package com.example.EarthquakeCheck.service;

import com.example.EarthquakeCheck.DTO.BuildingRequest;
import com.example.EarthquakeCheck.DTO.EvaluationResultDTO;
import java.util.List;

public interface EvaluationService {
    List<EvaluationResultDTO> getAllEvaluations();
    EvaluationResultDTO getEvaluationById(Long id);
    EvaluationResultDTO saveEvaluation(EvaluationResultDTO evaluationDTO);
    EvaluationResultDTO evaluateBuilding(BuildingRequest request);
} 