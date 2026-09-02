package com.codesupreme.boxingclub.api.notification.controller;

import com.codesupreme.boxingclub.dto.notification.NotificationDto;
import com.codesupreme.boxingclub.service.impl.notification.NotificationImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v10/notification")
public class NotificationController {

    private final NotificationImpl service;

    public NotificationController(NotificationImpl service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getAllNotification() {
        return ResponseEntity.ok(service.getAllNotification());
    }

    @GetMapping("/public")
    public ResponseEntity<List<NotificationDto>> getPublicNotifications() {
        return ResponseEntity.ok(service.getPublicNotifications());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<NotificationDto>> getNotificationsForCustomer(
            @PathVariable String customerId
    ) {
        return ResponseEntity.ok(service.getNotificationsForCustomer(customerId));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationDto> getNotificationById(
            @PathVariable("notificationId") Long id
    ) {
        NotificationDto det = service.getNotificationById(id);
        return det != null ? ResponseEntity.ok(det) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<NotificationDto> saveNotification(@RequestBody NotificationDto dto) {
        return ResponseEntity.ok(service.saveNotification(dto));
    }

    @PutMapping("/{notificationId}")
    public ResponseEntity<?> updateNotification(
            @PathVariable("notificationId") Long id,
            @RequestBody NotificationDto notificationDto
    ) {
        NotificationDto updatedNotification = service.updateNotification(id, notificationDto);
        return updatedNotification != null
                ? ResponseEntity.ok(updatedNotification)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(
            @PathVariable("notificationId") Long id
    ) {
        Boolean deleted = service.deleteNotification(id);
        return deleted
                ? ResponseEntity.ok("Admin notification deleted successfully")
                : ResponseEntity.notFound().build();
    }
}
