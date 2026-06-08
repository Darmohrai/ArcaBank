package com.arcabank.notification.controller;

import com.arcabank.notification.model.BaseNotification;
import com.arcabank.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        notificationRepository.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all/{userId}")
    public ResponseEntity<Void> markAllRead(@PathVariable UUID userId) {
        notificationRepository.markAllAsReadForUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history/{userId}")
    public List<BaseNotification> getHistory(
        @PathVariable UUID userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        int offset = page * size;
        return notificationRepository.findByUserId(userId, size, offset);
    }
}
