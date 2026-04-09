package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.dto.NotificationDTO;
import com.assetmanagement.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications() {
        System.out.println("GET /api/notifications called!");
        List<NotificationDTO> notifs = notificationService.getNotificationsForCurrentUser();
        System.out.println("Returned notifications size: " + notifs.size());
        for (NotificationDTO n : notifs) {
            System.out.println(" - " + n.getType() + " : " + n.getTitle());
        }
        return ResponseEntity.ok(notifs);
    }
}
