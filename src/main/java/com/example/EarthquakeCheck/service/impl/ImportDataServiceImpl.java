package com.example.EarthquakeCheck.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.EarthquakeCheck.entity.PgaValue;
import com.example.EarthquakeCheck.repository.PgaValueRepository;
import com.example.EarthquakeCheck.service.ImportDataService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImportDataServiceImpl implements ImportDataService {
    
    private final PgaValueRepository pgaValueRepository;
    
    private static final int BATCH_SIZE = 1000;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    
    @Override
    @Transactional
    public void importPgaData(MultipartFile file) {
        try {
            List<PgaValue> pgaValues = readExcelFile(file);
            savePgaValuesInBatches(pgaValues);
        } catch (IOException e) {
            throw new RuntimeException("Excel dosyası okunurken hata oluştu: " + e.getMessage());
        }
    }
    
    private void savePgaValuesInBatches(List<PgaValue> pgaValues) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < pgaValues.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, pgaValues.size());
            List<PgaValue> batch = pgaValues.subList(i, endIndex);
            
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                pgaValueRepository.saveAll(batch);
            }, executorService);
            
            futures.add(future);
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
    }
    
    private List<PgaValue> readExcelFile(MultipartFile file) throws IOException {
        List<PgaValue> pgaValues = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // İlk satırı başlık olarak atlayalım
            boolean isFirstRow = true;
            
            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }
                
                PgaValue pgaValue = new PgaValue();
                
                double longitude = getNumericCellValue(row.getCell(0));
                pgaValue.setLongitude(longitude);
                
                double latitude = getNumericCellValue(row.getCell(1));
                pgaValue.setLatitude(latitude);
                
                double dd1 = getNumericCellValue(row.getCell(2));
                pgaValue.setDd1(dd1);
                
                double dd2 = getNumericCellValue(row.getCell(3));
                pgaValue.setDd2(dd2);
                
                double dd3 = getNumericCellValue(row.getCell(4));
                pgaValue.setDd3(dd3);
                
                double dd4 = getNumericCellValue(row.getCell(5));
                pgaValue.setDd4(dd4);
                
                pgaValues.add(pgaValue);
            }
        }
        
        return pgaValues;
    }
    
    private double getNumericCellValue(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        try {
            return cell.getNumericCellValue();
        } catch (Exception e) {
            try {
                return Double.parseDouble(cell.getStringCellValue().replace(",", "."));
            } catch (Exception ex) {
                return 0.0;
            }
        }
    }
}
