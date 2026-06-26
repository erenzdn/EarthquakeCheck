package com.example.EarthquakeCheck.event;

import java.util.UUID;

public record ContactMessageCreatedEvent(UUID messageId, String email, String subject) {
}
