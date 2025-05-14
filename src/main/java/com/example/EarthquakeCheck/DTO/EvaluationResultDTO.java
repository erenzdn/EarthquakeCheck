package com.example.EarthquakeCheck.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EvaluationResultDTO {
    private Long id;
    private Long buildingId;
    private String safetyGrade;
    private String safetyGradePercentage;
    private String nearestAssemblyArea;
    private LocalDateTime evaluationDate;
    private String evaluationNotes;
} 