package com.example.EarthquakeCheck.repository;

import com.example.EarthquakeCheck.entity.EvaluationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationResultRepository extends JpaRepository<EvaluationResult, Long> {
}
