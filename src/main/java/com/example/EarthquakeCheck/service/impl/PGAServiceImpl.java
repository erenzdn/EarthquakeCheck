package com.example.EarthquakeCheck.service.impl;

import org.springframework.stereotype.Service;

import com.example.EarthquakeCheck.DTO.PgaValueDTO;
import com.example.EarthquakeCheck.entity.PgaValue;
import com.example.EarthquakeCheck.repository.PgaValueRepository;
import com.example.EarthquakeCheck.service.PGAService;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class PGAServiceImpl implements PGAService {
    
    private final PgaValueRepository pgaValueRepository;

    @Override
    public PgaValueDTO getPGAValue(double latitude, double longitude){
        double roundedLatitude = roundToNearestGridValue(latitude);
        double roundedLongitude = roundToNearestGridValue(longitude);
        
        PgaValue pgaValue = pgaValueRepository.findByLatitudeAndLongitude(roundedLatitude, roundedLongitude);
        
        if(pgaValue == null){
            pgaValue = pgaValueRepository.findNearestCoordinate(latitude, longitude);
        }
        
        if(pgaValue != null){
            PgaValueDTO pgaValueDTO = new PgaValueDTO(pgaValue);
            return pgaValueDTO;
        }

        return null;
    }
    
    /**
     * Verilen değeri en yakın grid değerine yuvarlar.
     * Grid aralığı 0.1 olarak kabul edilmiştir.
     */
    private double roundToNearestGridValue(double value) {
        double gridSpacing = 0.1;
        
        return Math.round(value / gridSpacing) * gridSpacing;
    }
}

