package com.example.EarthquakeCheck.service.impl;

import com.example.EarthquakeCheck.DTO.ContactRequest;
import com.example.EarthquakeCheck.DTO.ContactResponse;
import com.example.EarthquakeCheck.event.ContactMessageCreatedEvent;
import com.example.EarthquakeCheck.model.ContactMessage;
import com.example.EarthquakeCheck.model.ContactMessageStatus;
import com.example.EarthquakeCheck.repository.ContactMessageRepository;
import com.example.EarthquakeCheck.service.ContactMessageService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ContactResponse createMessage(ContactRequest request) {
        ContactMessage message = new ContactMessage();
        message.setFullName(request.fullName());
        message.setEmail(request.email());
        message.setSubject(request.subject());
        message.setMessage(request.message());
        message.setStatus(ContactMessageStatus.UNREAD);

        ContactMessage saved = contactMessageRepository.save(message);

        eventPublisher.publishEvent(new ContactMessageCreatedEvent(
                saved.getId(), saved.getEmail(), saved.getSubject()));
        log.info("Yeni iletisim mesaji kaydedildi. messageId={}, email={}", saved.getId(), saved.getEmail());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> getAllMessages(Pageable pageable) {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public ContactResponse markAsRead(UUID messageId) {
        ContactMessage message = contactMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesaj bulunamadi: " + messageId));

        message.setStatus(ContactMessageStatus.READ);
        ContactMessage updated = contactMessageRepository.save(message);
        return toResponse(updated);
    }

    private ContactResponse toResponse(ContactMessage message) {
        return new ContactResponse(
                message.getId(),
                message.getFullName(),
                message.getEmail(),
                message.getSubject(),
                message.getMessage(),
                message.getStatus(),
                message.getCreatedAt());
    }
}
