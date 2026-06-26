package com.example.EarthquakeCheck.util;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public final class XlsxFileValidator {

    private static final byte[] XLSX_MAGIC_BYTES = {0x50, 0x4B, 0x03, 0x04};

    private XlsxFileValidator() {}

    public static void validate(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Lutfen bir Excel dosyasi seciniz.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Sadece .xlsx dosyalari desteklenmektedir.");
        }

        byte[] header = file.getInputStream().readNBytes(XLSX_MAGIC_BYTES.length);
        if (header.length < XLSX_MAGIC_BYTES.length || !startsWithXlsxMagic(header)) {
            throw new IllegalArgumentException("Gecersiz Excel dosya formati.");
        }
    }

    private static boolean startsWithXlsxMagic(byte[] header) {
        for (int i = 0; i < XLSX_MAGIC_BYTES.length; i++) {
            if (header[i] != XLSX_MAGIC_BYTES[i]) {
                return false;
            }
        }
        return true;
    }
}
