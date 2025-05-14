package com.example.EarthquakeCheck.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImportDataService {
    
    void importPgaData(MultipartFile file);
}