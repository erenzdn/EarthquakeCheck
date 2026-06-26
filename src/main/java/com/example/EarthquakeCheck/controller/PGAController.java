package com.example.EarthquakeCheck.controller;

import com.example.EarthquakeCheck.DTO.LocationDTO;
import com.example.EarthquakeCheck.DTO.PgaValueDTO;
import com.example.EarthquakeCheck.service.PGAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pga")
@CrossOrigin(origins = "*")
public class PGAController {

    @Autowired
    private PGAService pgaService;

    @GetMapping("/value")
    public ResponseEntity<PgaValueDTO> getPGAValueFromQuery(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        try {
            PgaValueDTO pgaValue = pgaService.getPGAValue(latitude, longitude);
            return ResponseEntity.ok(pgaValue);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/value")
    public ResponseEntity<PgaValueDTO> getPGAValueFromBody(@RequestBody LocationDTO locationDTO) {
        try {
            PgaValueDTO pgaValue = pgaService.getPGAValue(locationDTO.getLatitude(), locationDTO.getLongitude());
            return ResponseEntity.ok(pgaValue);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
