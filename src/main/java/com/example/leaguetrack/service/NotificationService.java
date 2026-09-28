package com.example.leaguetrack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final Deque<ActivityLog> recentActivities = new ConcurrentLinkedDeque<>();
    private static final int MAX_LOGS = 30;

    public void notify(String eventType, String message) {
        log.info("[LEAGUE-TRACK NOTIFICATION] [{}] - {}", eventType, message);
        recentActivities.addFirst(new ActivityLog(eventType, message, LocalDateTime.now()));
        while (recentActivities.size() > MAX_LOGS) {
            recentActivities.pollLast();
        }
    }

    public List<ActivityLog> getRecentActivities() {
        return new ArrayList<>(recentActivities);
    }

    public static class ActivityLog {
        private String eventType;
        private String message;
        private LocalDateTime timestamp;

        public ActivityLog(String eventType, String message, LocalDateTime timestamp) {
            this.eventType = eventType;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getEventType() {
            return eventType;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}
