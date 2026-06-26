package com.example.EarthquakeCheck.service.impl;

import com.example.EarthquakeCheck.entity.PgaValue;
import com.example.EarthquakeCheck.repository.PgaValueRepository;
import com.example.EarthquakeCheck.service.ImportDataService;
import com.example.EarthquakeCheck.util.XlsxFileValidator;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportDataServiceImpl implements ImportDataService {

    private static final int BATCH_SIZE = 1000;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int MAX_ROWS = 500_000;
    private static final long MAX_ENTRY_SIZE_BYTES = 10L * 1024 * 1024;

    private final PgaValueRepository pgaValueRepository;

    @PostConstruct
    void configurePoiSecurityLimits() {
        ZipSecureFile.setMinInflateRatio(0.001d);
        ZipSecureFile.setMaxEntrySize(MAX_ENTRY_SIZE_BYTES);
        ZipSecureFile.setMaxTextSize(MAX_ENTRY_SIZE_BYTES);
    }

    @Override
    @Transactional
    public void importPgaData(MultipartFile file) {
        try {
            XlsxFileValidator.validate(file);
            List<PgaValue> pgaValues = readExcelFile(file);
            pgaValueRepository.truncateAll();
            savePgaValuesInBatches(pgaValues);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (IOException ex) {
            log.error("Excel dosyasi okunurken hata olustu", ex);
            throw new RuntimeException("Excel dosyasi okunurken hata olustu.");
        }
    }

    private void savePgaValuesInBatches(List<PgaValue> pgaValues) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < pgaValues.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, pgaValues.size());
            List<PgaValue> batch = pgaValues.subList(i, endIndex);

            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    () -> pgaValueRepository.saveAll(batch), executorService);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
    }

    private List<PgaValue> readExcelFile(MultipartFile file) throws IOException {
        List<PgaValue> pgaValues = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;
            int rowCount = 0;

            for (Row row : sheet) {
                if (rowCount > MAX_ROWS) {
                    throw new IllegalArgumentException("Excel dosyasi izin verilen satir limitini asti.");
                }

                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                PgaValue pgaValue = new PgaValue();
                pgaValue.setLongitude(getNumericCellValue(row.getCell(0)));
                pgaValue.setLatitude(getNumericCellValue(row.getCell(1)));
                pgaValue.setDd1(getNumericCellValue(row.getCell(2)));
                pgaValue.setDd2(getNumericCellValue(row.getCell(3)));
                pgaValue.setDd3(getNumericCellValue(row.getCell(4)));
                pgaValue.setDd4(getNumericCellValue(row.getCell(5)));

                pgaValues.add(pgaValue);
                rowCount++;
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
