package com.example.EarthquakeCheck.controller;

import com.example.EarthquakeCheck.service.ImportDataService;
import com.example.EarthquakeCheck.util.XlsxFileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportDataController {

    private final ImportDataService importDataService;

    @PostMapping("/pga")
    public ResponseEntity<String> importPgaData(@RequestParam("file") MultipartFile file) {
        try {
            XlsxFileValidator.validate(file);
            importDataService.importPgaData(file);
            return ResponseEntity.ok("PGA verileri basariyla ice aktarildi");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            log.error("PGA veri ice aktarma hatasi", ex);
            return ResponseEntity.internalServerError().body("Veri ice aktarma sirasinda hata olustu.");
        }
    }
}
