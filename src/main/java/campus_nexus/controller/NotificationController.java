package campus_nexus.controller;

import campus_nexus.dto.response.NotificationResponseDTO;
import campus_nexus.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing Notifications.
 * Provides endpoints for users to view, read, and manage their notifications.
 * Features: notification panel, unread badge, mark as read.
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    /**
     * GET /api/notifications - Get all notifications for logged-in user
     * Query params: page, size
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        logger.info("GET /api/notifications - User ID: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationResponseDTO> notificationPage = notificationService.getUserNotifications(userId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", notificationPage.getContent());
        response.put("currentPage", notificationPage.getNumber());
        response.put("totalItems", notificationPage.getTotalElements());
        response.put("totalPages", notificationPage.getTotalPages());
        response.put("pageSize", notificationPage.getSize());
        response.put("hasNext", notificationPage.hasNext());
        response.put("hasPrevious", notificationPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/notifications/unread - Get unread notifications only
     */
    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponseDTO>> getUnreadNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        logger.info("GET /api/notifications/unread - User ID: {}", userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationResponseDTO> notifications = notificationService.getUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/notifications/unread/count - Get unread notification count (for badge)
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestHeader("X-User-Id") Long userId) {
        logger.info("GET /api/notifications/unread/count - User ID: {}", userId);

        long count = notificationService.getUnreadCount(userId);

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/notifications/{id}/read - Mark a specific notification as read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        logger.info("PATCH /api/notifications/{}/read - User ID: {}", id, userId);
        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/notifications/read-all - Mark all notifications as read
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestHeader("X-User-Id") Long userId) {
        logger.info("PATCH /api/notifications/read-all - User ID: {}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/notifications/{id} - Delete a specific notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        logger.info("DELETE /api/notifications/{} - User ID: {}", id, userId);
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/notifications - Delete all notifications for user
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications(@RequestHeader("X-User-Id") Long userId) {
        logger.info("DELETE /api/notifications - User ID: {}", userId);
        notificationService.deleteAllNotifications(userId);
        return ResponseEntity.noContent().build();
    }
}