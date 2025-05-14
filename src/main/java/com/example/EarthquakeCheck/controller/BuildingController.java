package com.example.EarthquakeCheck.controller;

import org.springframework.web.bind.annotation.RestController;
import com.example.EarthquakeCheck.DTO.BuildingRequest;
import com.example.EarthquakeCheck.DTO.EvaluationResultDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.EarthquakeCheck.service.EvaluationService;





@RestController
@RequestMapping("/api/building")
@CrossOrigin(origins = "*")// gerekirse frontend işlemi için
public class BuildingController {
        
    @Autowired
    private EvaluationService evaluationService;

    @PostMapping("/evaluate")
    public EvaluationResultDTO evaluationBuilding(@RequestBody BuildingRequest request){
        return evaluationService.evaluateBuilding(request); 
    }
    
}
