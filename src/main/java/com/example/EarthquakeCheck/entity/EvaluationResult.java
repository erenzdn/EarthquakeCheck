package com.example.EarthquakeCheck.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "evaluation_results")
public class EvaluationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;

    private String safetyGrade;
    private String nearestAssemblyArea;
    private LocalDateTime evaluationDate;
    private String evaluationNotes;
} 