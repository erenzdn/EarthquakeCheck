package com.example.EarthquakeCheck.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank(message = "Ad soyad zorunludur.")
        @Size(max = 120, message = "Ad soyad en fazla 120 karakter olabilir.")
        String fullName,

        @NotBlank(message = "E-posta zorunludur.")
        @Email(message = "Gecerli bir e-posta adresi giriniz.")
        @Size(max = 255, message = "E-posta en fazla 255 karakter olabilir.")
        String email,

        @NotBlank(message = "Konu zorunludur.")
        @Size(max = 150, message = "Konu en fazla 150 karakter olabilir.")
        String subject,

        @NotBlank(message = "Mesaj icerigi zorunludur.")
        @Size(min = 10, max = 4000, message = "Mesaj 10-4000 karakter araliginda olmalidir.")
        String message
) {
}
