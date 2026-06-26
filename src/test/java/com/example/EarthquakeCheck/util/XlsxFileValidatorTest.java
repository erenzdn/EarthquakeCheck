package com.example.EarthquakeCheck.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class XlsxFileValidatorTest {

    @Test
    void shouldAcceptValidXlsxFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "data.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[] {0x50, 0x4B, 0x03, 0x04, 0x00});

        assertDoesNotThrow(() -> XlsxFileValidator.validate(file));
    }

    @Test
    void shouldRejectXlsExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "data.xls",
                "application/vnd.ms-excel",
                new byte[] {0x50, 0x4B, 0x03, 0x04});

        assertThrows(IllegalArgumentException.class, () -> XlsxFileValidator.validate(file));
    }

    @Test
    void shouldRejectInvalidMagicBytes() {
        MockMultipartFile file =
                new MockMultipartFile("file", "data.xlsx", "application/octet-stream", new byte[] {0x00, 0x01, 0x02});

        assertThrows(IllegalArgumentException.class, () -> XlsxFileValidator.validate(file));
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "data.xlsx", "application/octet-stream", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> XlsxFileValidator.validate(file));
    }
}
