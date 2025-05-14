package com.example.EarthquakeCheck.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.EarthquakeCheck.service.ImportDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportDataController {
    
    private final ImportDataService importDataService;    

    @PostMapping("/pga")
    public ResponseEntity<String> importPgaData(@RequestParam("file") MultipartFile file){
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Lütfen bir Excel dosyası seçiniz");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !(originalFilename.endsWith(".xlsx") || originalFilename.endsWith(".xls"))) {
            return ResponseEntity.badRequest().body("Sadece Excel dosyaları (.xlsx, .xls) desteklenmektedir");
        }
        
        try {
            importDataService.importPgaData(file);
            return ResponseEntity.ok("PGA verileri başarıyla içe aktarıldı");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Veri içe aktarma sırasında hata oluştu: " + e.getMessage());
        }
    }
}
