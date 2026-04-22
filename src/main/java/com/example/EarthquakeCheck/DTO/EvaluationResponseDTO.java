package com.example.EarthquakeCheck.DTO;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponseDTO {
    private Long id;
    private String riskClass;
    private String message;
    private Integer safetyGradePercentage;
    private LocalDateTime evaluatedAt;
}
