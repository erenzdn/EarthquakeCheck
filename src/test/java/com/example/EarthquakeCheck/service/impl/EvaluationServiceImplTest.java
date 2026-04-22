package com.example.EarthquakeCheck.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.EarthquakeCheck.DTO.BuildingRequest;
import com.example.EarthquakeCheck.DTO.EvaluationResponseDTO;
import com.example.EarthquakeCheck.model.Building;
import com.example.EarthquakeCheck.model.EvaluationResult;
import com.example.EarthquakeCheck.repository.BuildingRepository;
import com.example.EarthquakeCheck.repository.EvaluationResultRepository;
import com.example.EarthquakeCheck.service.GeocodingService;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceImplTest {

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private EvaluationResultRepository evaluationResultRepository;

    @Mock
    private GeocodingService geocodingService;

    @InjectMocks
    private EvaluationServiceImpl evaluationService;

    @Test
    void shouldHandleZeroFloorAndZeroAgeEdgeCase() {
        int currentYear = Year.now().getValue();
        BuildingRequest request = new BuildingRequest(currentYear, 0, "Test adres", "Konut", 39.0, 35.0);
        when(buildingRepository.save(any(Building.class))).thenAnswer(invocation -> {
            Building building = invocation.getArgument(0);
            building.setId(10L);
            return building;
        });
        when(evaluationResultRepository.save(any(EvaluationResult.class))).thenAnswer(invocation -> {
            EvaluationResult result = invocation.getArgument(0);
            result.setId(101L);
            result.setEvaluatedAt(LocalDateTime.now());
            return result;
        });

        EvaluationResponseDTO response = evaluationService.evaluateBuilding(request);

        assertNotNull(response);
        assertEquals(87, response.getSafetyGradePercentage());
        assertEquals("B", response.getRiskClass());
    }

    @Test
    void shouldClampScoreForVeryOldAndVeryTallBuilding() {
        int yearBuilt = Year.now().getValue() - 150;
        BuildingRequest request = new BuildingRequest(yearBuilt, 150, "Eski bina", "Apartman", 40.0, 30.0);
        when(buildingRepository.save(any(Building.class))).thenAnswer(invocation -> {
            Building building = invocation.getArgument(0);
            building.setId(11L);
            return building;
        });
        when(evaluationResultRepository.save(any(EvaluationResult.class))).thenAnswer(invocation -> {
            EvaluationResult result = invocation.getArgument(0);
            result.setId(102L);
            result.setEvaluatedAt(LocalDateTime.now());
            return result;
        });

        EvaluationResponseDTO response = evaluationService.evaluateBuilding(request);

        assertNotNull(response);
        assertEquals(34, response.getSafetyGradePercentage());
        assertEquals("E", response.getRiskClass());
    }

    @Test
    void shouldPersistBuildingAssociationWithEvaluationResult() {
        int yearBuilt = Year.now().getValue() - 20;
        BuildingRequest request = new BuildingRequest(yearBuilt, 7, "Bagli bina", "Konut", null, null);
        when(geocodingService.getCoordinatesFromAddress(any(String.class)))
                .thenReturn(Optional.of(new GeocodingService.CoordinatePair(41.0082, 28.9784)));
        when(buildingRepository.save(any(Building.class))).thenAnswer(invocation -> {
            Building building = invocation.getArgument(0);
            building.setId(42L);
            return building;
        });
        when(evaluationResultRepository.save(any(EvaluationResult.class))).thenAnswer(invocation -> {
            EvaluationResult result = invocation.getArgument(0);
            result.setId(103L);
            result.setEvaluatedAt(LocalDateTime.now());
            return result;
        });

        evaluationService.evaluateBuilding(request);

        ArgumentCaptor<EvaluationResult> resultCaptor = ArgumentCaptor.forClass(EvaluationResult.class);
        verify(evaluationResultRepository).save(resultCaptor.capture());
        assertEquals(42L, resultCaptor.getValue().getBuilding().getId());
    }
}
