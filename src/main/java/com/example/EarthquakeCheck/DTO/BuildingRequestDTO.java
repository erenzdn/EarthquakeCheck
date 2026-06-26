package com.example.EarthquakeCheck.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuildingRequestDTO {

    @NotNull(message = "katSayisi zorunludur.")
    @Min(value = 1, message = "katSayisi en az 1 olabilir.")
    @Max(value = 100, message = "katSayisi en fazla 100 olabilir.")
    private Integer katSayisi;

    @NotNull(message = "yapimYili zorunludur.")
    @Min(value = 1800, message = "yapimYili en az 1800 olabilir.")
    @Max(value = 2026, message = "yapimYili en fazla 2026 olabilir.")
    private Integer yapimYili;
}
