package com.example.EarthquakeCheck.service.impl;

import com.example.EarthquakeCheck.DTO.BuildingRequest;
import com.example.EarthquakeCheck.DTO.EvaluationResponseDTO;
import com.example.EarthquakeCheck.model.Building;
import com.example.EarthquakeCheck.model.EvaluationResult;
import com.example.EarthquakeCheck.repository.BuildingRepository;
import com.example.EarthquakeCheck.repository.EvaluationResultRepository;
import com.example.EarthquakeCheck.service.EvaluationService;
import com.example.EarthquakeCheck.service.GeocodingService;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    private static final double DEFAULT_LATITUDE = 39.0;
    private static final double DEFAULT_LONGITUDE = 35.0;

    private final BuildingRepository buildingRepository;
    private final EvaluationResultRepository evaluationResultRepository;
    private final GeocodingService geocodingService;

    @Override
    @Transactional
    public EvaluationResponseDTO evaluateBuilding(BuildingRequest request) {
        int buildingAge = calculateBuildingAge(request.getYearBuilt());
        CoordinateInput coordinateInput = resolveCoordinates(request);
        Building savedBuilding = buildingRepository.save(toBuildingEntity(request, coordinateInput));

        int ageScore = calculateAgeScore(buildingAge);
        int floorScore = calculateFloorScore(request.getFloorCount());

        double pgaFactor = simulatePgaFactor(coordinateInput.latitude(), coordinateInput.longitude());
        int rawScore = (int) Math.round((ageScore + floorScore) * pgaFactor);
        int safetyGradePercentage = clampSafetyScore(100 - rawScore);

        String riskClass = classifyRisk(safetyGradePercentage);
        String message = buildRiskMessage(riskClass);

        EvaluationResult evaluationResult = EvaluationResult.builder()
                .riskClass(riskClass)
                .message(message)
                .safetyGradePercentage(safetyGradePercentage)
                .evaluatedAt(LocalDateTime.now())
                .building(savedBuilding)
                .build();

        log.info("Bina degerlendirmesi olusturuluyor. buildingId={}, riskClass={}, safetyScore={}",
                savedBuilding.getId(), riskClass, safetyGradePercentage);
        EvaluationResult savedResult = evaluationResultRepository.save(evaluationResult);
        return toResponseDto(savedResult);
    }

    private Building toBuildingEntity(BuildingRequest request, CoordinateInput coordinateInput) {
        Building building = new Building();
        building.setAddress(request.getAddress());
        building.setBuildingType(request.getBuildingType());
        building.setYearBuilt(request.getYearBuilt());
        building.setLatitude(coordinateInput.latitude());
        building.setLongitude(coordinateInput.longitude());
        return building;
    }

    private int calculateBuildingAge(Integer yearBuilt) {
        int currentYear = Year.now().getValue();
        return Math.max(0, currentYear - yearBuilt);
    }

    private CoordinateInput resolveCoordinates(BuildingRequest request) {
        Double latitude = request.getLatitude();
        Double longitude = request.getLongitude();

        if (latitude != null && longitude != null) {
            return new CoordinateInput(latitude, longitude);
        }

        if (Objects.nonNull(request.getAddress())) {
            return geocodingService.getCoordinatesFromAddress(request.getAddress())
                    .map(pair -> {
                        log.info("Adres koordinata cevrildi. address={}, lat={}, lon={}",
                                request.getAddress(), pair.latitude(), pair.longitude());
                        return new CoordinateInput(pair.latitude(), pair.longitude());
                    })
                    .orElseGet(() -> {
                        log.warn("Adres koordinata cevrilemedi, varsayilan koordinatlara donuluyor. address={}",
                                request.getAddress());
                        return new CoordinateInput(latitude, longitude);
                    });
        }

        return new CoordinateInput(latitude, longitude);
    }

    private int calculateAgeScore(Integer buildingAge) {
        if (buildingAge > 30) {
            return 30;
        }
        if (buildingAge >= 20) {
            return 20;
        }
        if (buildingAge >= 10) {
            return 10;
        }
        return 5;
    }

    private int calculateFloorScore(Integer floorCount) {
        if (floorCount > 10) {
            return 20;
        }
        if (floorCount >= 7) {
            return 15;
        }
        if (floorCount >= 4) {
            return 10;
        }
        return 5;
    }

    private double simulatePgaFactor(Double latitude, Double longitude) {
        // TODO: Replace with external PGA service integration.
        double effectiveLat = latitude != null ? latitude : DEFAULT_LATITUDE;
        double effectiveLon = longitude != null ? longitude : DEFAULT_LONGITUDE;
        double coordinateSignal = Math.abs(effectiveLat) + Math.abs(effectiveLon);
        double normalizedSignal = Math.min(1.0, coordinateSignal / 180.0);
        return 1.0 + (normalizedSignal * 0.8);
    }

    private String classifyRisk(int safetyGradePercentage) {
        if (safetyGradePercentage >= 90) {
            return "A";
        }
        if (safetyGradePercentage >= 75) {
            return "B";
        }
        if (safetyGradePercentage >= 60) {
            return "C";
        }
        if (safetyGradePercentage >= 45) {
            return "D";
        }
        if (safetyGradePercentage >= 30) {
            return "E";
        }
        return "F";
    }

    private String buildRiskMessage(String riskClass) {
        return switch (riskClass) {
            case "A" -> "Cok guvenli bina profili. Rutin denetim yeterli.";
            case "B" -> "Guvenli profil korunuyor. Periyodik kontrol onerilir.";
            case "C" -> "Orta duzey guvenlik. Detayli teknik inceleme planlanmali.";
            case "D" -> "Risk artiyor. Guclendirme ihtiyaci degerlendirilmeli.";
            case "E" -> "Yuksek riskli profil. Muhendislik analizi ve hizli aksiyon onerilir.";
            default -> "Cok riskli profil. Acil guclendirme veya tahliye degerlendirilmelidir.";
        };
    }

    private EvaluationResponseDTO toResponseDto(EvaluationResult evaluationResult) {
        return EvaluationResponseDTO.builder()
                .id(evaluationResult.getId())
                .riskClass(evaluationResult.getRiskClass())
                .message(evaluationResult.getMessage())
                .safetyGradePercentage(evaluationResult.getSafetyGradePercentage())
                .evaluatedAt(evaluationResult.getEvaluatedAt())
                .build();
    }

    private int clampSafetyScore(int safetyScore) {
        return Math.max(0, Math.min(100, safetyScore));
    }

    private record CoordinateInput(Double latitude, Double longitude) {
    }
}
