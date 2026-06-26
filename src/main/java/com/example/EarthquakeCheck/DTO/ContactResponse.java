package com.example.EarthquakeCheck.DTO;

import com.example.EarthquakeCheck.model.ContactMessageStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ContactResponse(
        UUID id,
        String fullName,
        String email,
        String subject,
        String message,
        ContactMessageStatus status,
        LocalDateTime createdAt
) {
}
