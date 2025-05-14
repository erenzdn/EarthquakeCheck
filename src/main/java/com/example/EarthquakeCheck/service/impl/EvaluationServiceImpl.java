package com.example.EarthquakeCheck.service.impl;

import com.example.EarthquakeCheck.DTO.BuildingRequest;
import com.example.EarthquakeCheck.DTO.EvaluationResultDTO;
import com.example.EarthquakeCheck.DTO.PgaValueDTO;
import com.example.EarthquakeCheck.entity.PgaValue;
import com.example.EarthquakeCheck.repository.PgaValueRepository;
import com.example.EarthquakeCheck.service.EvaluationService;
import com.example.EarthquakeCheck.service.PGAService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

        private final PGAService pgaService;

    @Override
    public List<EvaluationResultDTO> getAllEvaluations() {
        // TODO: Implement
        return null;
    }

    @Override
    public EvaluationResultDTO getEvaluationById(Long id) {
        // TODO: Implement
        return null;
    }

    @Override
    public EvaluationResultDTO saveEvaluation(EvaluationResultDTO evaluationDTO) {
        // TODO: Implement
        return null;
    }

    @Override
    public EvaluationResultDTO evaluateBuilding(BuildingRequest request) {
        Double safetyGrade = 100d;
        
        int buildingAge = request.getBuildingAge();
        
      
        if (buildingAge  < 2018) 
            safetyGrade = 80d; 
        else if (buildingAge < 2007) 
            safetyGrade = 50d; 
        else if (buildingAge < 1998) 
            safetyGrade = 20d; 
        else if (buildingAge < 1975) 
            safetyGrade = 5d; 
        
        int floorCount = request.getFloorCount();
        double floorMultiplier;
        
        switch (floorCount) {
            case 1:
                floorMultiplier = 1.20;
                break;
            case 2:
                floorMultiplier = 1.10;
                break;
            case 3:
                floorMultiplier = 1.05;
                break;
            case 4:
                floorMultiplier = 1.00;
                break;
            case 5:
                floorMultiplier = 0.95;
                break;
            default:
                floorMultiplier = Math.max(0.5, 1 - (floorCount * 0.01));
                break;
        }
        
        safetyGrade *= floorMultiplier;
        
        PgaValueDTO pgaValueDTO = pgaService.getPGAValue(request.getLocation().getLatitude(), request.getLocation().getLongitude());
        
        if (pgaValueDTO != null) {
            double dd2 = pgaValueDTO.getDd2();
            double pgaMultiplier = calculatePgaMultiplier(dd2);
            safetyGrade *= pgaMultiplier;
        }
        
        safetyGrade = Math.min(100.0, Math.max(0.0, safetyGrade));

        String nearestAssemblyArea = "örnek toplanma alani";
        String safetyGradePercentage = String.format("%.1f", safetyGrade);
        
        String letterGrade = calculateLetterGrade(safetyGrade);
        
        EvaluationResultDTO result = new EvaluationResultDTO();
        result.setNearestAssemblyArea(nearestAssemblyArea);
        result.setSafetyGrade(letterGrade);
        result.setSafetyGradePercentage(safetyGradePercentage);
        
        return result;
    }

    /**
     * PGA dd2 değerine göre güvenlik puanı çarpanını hesaplar
     * @param dd2 PGA dd2 değeri
     * @return Güvenlik puanı çarpanı
     */
    private double calculatePgaMultiplier(double dd2) {
        if (dd2 <= 0.1) 
            return 1.0; 
        else if (dd2 <= 0.2) 
            return 0.95; 
        else if (dd2 <= 0.3) 
            return 0.90; 
        else if (dd2 <= 0.4) 
            return 0.85; 
        else if (dd2 <= 0.5) 
            return 0.80; 
        else if (dd2 <= 0.6) 
            return 0.75; 
        else 
            return 0.70; 
        
    }

    /**
     * Güvenlik puanından harf notu hesaplar
     * @param safetyGrade Güvenlik puanı (0-100)
     * @return Harf notu (A, B, C, D, E, F)
     */
    private String calculateLetterGrade(double safetyGrade) {
        if (safetyGrade >= 90) {
            return "A";
        } else if (safetyGrade >= 80) {
            return "B";
        } else if (safetyGrade >= 70) {
            return "C";
        } else if (safetyGrade >= 50) {
            return "D";
        } else if (safetyGrade >= 30) {
            return "E";
        } else {
            return "F";
        }
    }
}