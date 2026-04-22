package com.example.EarthquakeCheck.controller;

import com.example.EarthquakeCheck.DTO.LocationDTO;
import com.example.EarthquakeCheck.DTO.PgaValueDTO;
import com.example.EarthquakeCheck.service.PGAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pga")
@CrossOrigin(origins = "*")
public class PGAController {

    @Autowired
    private PGAService pgaService;

    @GetMapping("/value")
    public ResponseEntity<PgaValueDTO> getPGAValue(@RequestBody LocationDTO locationDTO) {
        try {
            double latitude = locationDTO.getLatitude();
            double longitude = locationDTO.getLongitude();
            PgaValueDTO pgaValue = pgaService.getPGAValue(latitude, longitude);
            return ResponseEntity.ok(pgaValue);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
