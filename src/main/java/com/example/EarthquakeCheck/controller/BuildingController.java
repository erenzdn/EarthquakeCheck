package com.example.EarthquakeCheck.controller;

import com.example.EarthquakeCheck.DTO.BuildingRequest;
import com.example.EarthquakeCheck.DTO.EvaluationResponseDTO;
import com.example.EarthquakeCheck.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/building")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
@RequiredArgsConstructor
public class BuildingController {

    private final EvaluationService evaluationService;

    @PostMapping("/evaluate")
    @Operation(
            summary = "Bina deprem guvenlik degerlendirmesi yapar",
            description = "Bina yasi, kat sayisi ve istege bagli koordinat bilgileriyle guvenlik puani hesaplar ve sonucu veritabanina kaydeder."
    )
    public EvaluationResponseDTO evaluateBuilding(@Valid @RequestBody BuildingRequest request) {

        return evaluationService.evaluateBuilding(request); 
    }
    
}
