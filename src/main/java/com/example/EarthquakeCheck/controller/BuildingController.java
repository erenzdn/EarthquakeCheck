package com.example.EarthquakeCheck.controller;

import com.example.EarthquakeCheck.DTO.BuildingRequestDTO;
import com.example.EarthquakeCheck.DTO.BuildingRequest;
import com.example.EarthquakeCheck.DTO.EvaluationResponseDTO;
import com.example.EarthquakeCheck.model.Building;
import com.example.EarthquakeCheck.service.BuildingManagementService;
import com.example.EarthquakeCheck.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/building")
@RequiredArgsConstructor
public class BuildingController {

    private final EvaluationService evaluationService;
    private final BuildingManagementService buildingManagementService;

    @PostMapping
    @Operation(summary = "Bina kaydi olusturur")
    public Building createBuilding(@Valid @RequestBody BuildingRequestDTO requestDTO) {
        return buildingManagementService.createBuilding(requestDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Bina kaydini gunceller")
    public Building updateBuilding(@PathVariable Long id, @Valid @RequestBody BuildingRequestDTO requestDTO) {
        return buildingManagementService.updateBuilding(id, requestDTO);
    }

    @PostMapping("/evaluate")
    @Operation(
            summary = "Bina deprem guvenlik degerlendirmesi yapar",
            description = "Bina yasi, kat sayisi ve istege bagli koordinat bilgileriyle guvenlik puani hesaplar ve sonucu veritabanina kaydeder."
    )
    public EvaluationResponseDTO evaluateBuilding(@Valid @RequestBody BuildingRequest request) {

        return evaluationService.evaluateBuilding(request); 
    }
    
}
