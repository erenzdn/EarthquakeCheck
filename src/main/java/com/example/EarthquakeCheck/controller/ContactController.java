package com.example.EarthquakeCheck.controller;

import com.example.EarthquakeCheck.DTO.ContactRequest;
import com.example.EarthquakeCheck.DTO.ContactResponse;
import com.example.EarthquakeCheck.service.AdminAuthorizationService;
import com.example.EarthquakeCheck.service.ContactMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Tag(name = "Contact", description = "Iletisim mesajlari icin API")
public class ContactController {

    private static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";
    private final ContactMessageService contactMessageService;
    private final AdminAuthorizationService adminAuthorizationService;

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Yeni iletisim mesaji olusturur")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mesaj basariyla kaydedildi"),
            @ApiResponse(responseCode = "400", description = "Validation hatasi", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "429", description = "Rate limit asildi", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ContactResponse createContactMessage(@Valid @RequestBody ContactRequest request) {
        return contactMessageService.createMessage(request);
    }

    @GetMapping("/admin/messages")
    @Operation(summary = "Tum iletisim mesajlarini tarihe gore listeler (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mesajlar listelendi"),
            @ApiResponse(responseCode = "403", description = "Admin yetkisi yok", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public Page<ContactResponse> listAllMessages(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader(name = ADMIN_TOKEN_HEADER, required = false) String adminToken) {
        adminAuthorizationService.validateAdminToken(adminToken);
        return contactMessageService.getAllMessages(pageable);
    }

    @PatchMapping("/admin/messages/{id}/read")
    @Operation(summary = "Mesaji OKUNDU olarak isaretler (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mesaj okundu olarak guncellendi"),
            @ApiResponse(responseCode = "403", description = "Admin yetkisi yok", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Mesaj bulunamadi", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ContactResponse markMessageAsRead(
            @PathVariable("id") UUID messageId,
            @RequestHeader(name = ADMIN_TOKEN_HEADER, required = false) String adminToken) {
        adminAuthorizationService.validateAdminToken(adminToken);
        return contactMessageService.markAsRead(messageId);
    }
}
