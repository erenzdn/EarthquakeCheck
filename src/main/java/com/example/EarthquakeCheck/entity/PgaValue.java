package com.example.EarthquakeCheck.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data   
@Table(
    name = "pga_value",
    indexes = {
        @Index(name = "idx_pga_lat_long", columnList = "latitude,longitude")
    }
)
public class PgaValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double latitude;
    private double longitude;
    private double dd1;
    private double dd2;
    private double dd3;
    private double dd4;
}
