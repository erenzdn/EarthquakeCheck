package com.example.EarthquakeCheck.DTO;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.Year;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildingRequest {
    @NotNull(message = "yearBuilt zorunludur.")
    @Min(value = 1800, message = "yearBuilt 1800'den kucuk olamaz.")
    @Max(value = 2100, message = "yearBuilt 2100'den buyuk olamaz.")
    private Integer yearBuilt;

    @NotNull(message = "floorCount zorunludur.")
    @PositiveOrZero(message = "floorCount 0'dan kucuk olamaz.")
    @Max(value = 150, message = "floorCount 150'den buyuk olamaz.")
    private Integer floorCount;

    @Size(max = 500, message = "address en fazla 500 karakter olabilir.")
    private String address;

    @Size(max = 100, message = "buildingType en fazla 100 karakter olabilir.")
    private String buildingType;

    // Koordinat validasyonu production hatalarini azaltir.
    @Min(value = -90, message = "latitude -90'dan kucuk olamaz.")
    @Max(value = 90, message = "latitude 90'dan buyuk olamaz.")
    private Double latitude;

    @Min(value = -180, message = "longitude -180'den kucuk olamaz.")
    @Max(value = 180, message = "longitude 180'den buyuk olamaz.")
    private Double longitude;

    @JsonSetter("buildingAge")
    public void setBuildingAge(Integer buildingAge) {
        if (buildingAge == null || this.yearBuilt != null) {
            return;
        }
        this.yearBuilt = Year.now().getValue() - buildingAge;
    }
}
