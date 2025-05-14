package com.example.EarthquakeCheck.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        try {
            // Buildings tablosunu oluştur
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS buildings (
                    id BIGSERIAL PRIMARY KEY,
                    address VARCHAR(255),
                    building_age INTEGER,
                    floor_count INTEGER,
                    latitude DOUBLE PRECISION,
                    longitude DOUBLE PRECISION,
                    safety_grade VARCHAR(10),
                    nearest_assembly_area VARCHAR(255)
                )
            """);

            // Evaluation Results tablosunu oluştur
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS evaluation_results (
                    id BIGSERIAL PRIMARY KEY,
                    building_id BIGINT REFERENCES buildings(id),
                    safety_grade VARCHAR(10),
                    nearest_assembly_area VARCHAR(255),
                    evaluation_date TIMESTAMP,
                    evaluation_notes TEXT
                )
            """);

            System.out.println("Veritabanı tabloları başarıyla oluşturuldu.");
        } catch (Exception e) {
            System.err.println("Veritabanı tabloları oluşturulurken hata oluştu: " + e.getMessage());
        }
    }
} 