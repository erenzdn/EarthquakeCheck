package com.example.EarthquakeCheck.DTO;

import com.example.EarthquakeCheck.entity.PgaValue;

import lombok.Data;

@Data
public class PgaValueDTO {
    private Long id;
    private double latitude;
    private double longitude;
    private double dd1;
    private double dd2;
    private double dd3;
    private double dd4;

    public PgaValueDTO(PgaValue pgaValue){
        this.id = pgaValue.getId();
        this.latitude = pgaValue.getLatitude();
        this.longitude = pgaValue.getLongitude();
        this.dd1 = pgaValue.getDd1();
        this.dd2 = pgaValue.getDd2();
        this.dd3 = pgaValue.getDd3();
        this.dd4 = pgaValue.getDd4();
    }
}
