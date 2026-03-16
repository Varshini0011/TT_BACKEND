package com.example.BE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    // Get all notifications for a user
    @GetMapping("/{username}")
    public List<Notification> getNotifications(@PathVariable String username) {
        return notificationRepository
                .findByRecipientUsernameOrderByCreatedAtDesc(username);
    }

    // Get unread count
    @GetMapping("/{username}/count")
    public Map<String, Long> getUnreadCount(@PathVariable String username) {
        long count = notificationRepository
                .countByRecipientUsernameAndIsReadFalse(username);
        return Map.of("count", count);
    }

    // Mark all as read
    @PutMapping("/{username}/read-all")
    public void markAllRead(@PathVariable String username) {
        List<Notification> unread = notificationRepository
                .findByRecipientUsernameAndIsReadFalse(username);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // Mark one as read
    @PutMapping("/{id}/read")
    public Notification markRead(@PathVariable Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        return notificationRepository.save(n);
    }
}
