package com.example.EarthquakeCheck.DTO;

import lombok.Data;
import java.util.List;

@Data
public class BuildingDTO {
    private Long id;
    private String address;
    private int buildingAge;
    private int floorCount;
    private double latitude;
    private double longitude;
    private String safetyGrade;
    private String nearestAssemblyArea;
    private List<EvaluationResultDTO> evaluationResults;
} 