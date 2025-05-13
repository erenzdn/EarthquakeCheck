package com.example.EarthquakeCheck.service;

import com.example.EarthquakeCheck.DTO.BuildingRequest;
import com.example.EarthquakeCheck.DTO.EvaluationResult;
import com.example.EarthquakeCheck.model.Building;
import com.example.EarthquakeCheck.repository.EvaluationResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EvaluationServiceImpl implements EvaluationService {
    private final EvaluationResultRepository evaluationResultRepository;

    @Autowired
    public EvaluationServiceImpl(EvaluationResultRepository evaluationResultRepository) {
        this.evaluationResultRepository = evaluationResultRepository;
    }

    public EvaluationResult evaluateBuilding(BuildingRequest request) {
        
        //Buraya PGA değeri alma, sınıflandırma ve değerlendirme algoritması eklenecek
        return null;
    }
}
