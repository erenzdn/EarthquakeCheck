package com.example.EarthquakeCheck.service;

import com.example.EarthquakeCheck.DTO.PgaValueDTO;

public interface PGAService {

    PgaValueDTO getPGAValue(double latitude, double longitude);

}
