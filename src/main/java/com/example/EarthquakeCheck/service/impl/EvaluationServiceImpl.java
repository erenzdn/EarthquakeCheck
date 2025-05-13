package com.example.EarthquakeCheck.service.impl;

import org.springframework.stereotype.Service;
import com.example.EarthquakeCheck.DTO.BuildingRequest;
import com.example.EarthquakeCheck.service.EvaluationService;
import com.example.EarthquakeCheck.DTO.EvaluationResult;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    @Override
    public EvaluationResult evaluateBuilding(BuildingRequest request) {
        // Geçici örnek mantık (daha sonra gerçek algoritmayı buraya entegre edeceğiz)
        String safetyGrade;

        if(request.getBuildingAge() > 30 || request.getFloorCount() > 10){
            safetyGrade = "D";
        } else {
            safetyGrade = "B";
        }
    
        String nearestAssemblyArea = "örnek toplanma alani";

        return new EvaluationResult(safetyGrade, nearestAssemblyArea);
    }
}