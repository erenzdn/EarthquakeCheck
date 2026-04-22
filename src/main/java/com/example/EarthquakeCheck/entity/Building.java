package com.example.EarthquakeCheck.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "building")
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;
    private int buildingAge;
    private int floorCount;
    private double latitude;
    private double longitude;
    private String safetyGrade;
    private String nearestAssemblyArea;

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL)
    private List<EvaluationResult> evaluationResults;
} 