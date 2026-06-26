package com.example.EarthquakeCheck.service;

import com.example.EarthquakeCheck.DTO.ContactRequest;
import com.example.EarthquakeCheck.DTO.ContactResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactMessageService {
    ContactResponse createMessage(ContactRequest request);

    Page<ContactResponse> getAllMessages(Pageable pageable);

    ContactResponse markAsRead(UUID messageId);
}
