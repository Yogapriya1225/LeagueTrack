package com.example.leaguetrack.controller;

import com.example.leaguetrack.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
public class ActivityController {

    private final NotificationService notificationService;

    public ActivityController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationService.ActivityLog>> getRecentActivities() {
        return ResponseEntity.ok(notificationService.getRecentActivities());
    }
}
