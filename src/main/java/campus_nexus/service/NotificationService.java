package campus_nexus.service;

import campus_nexus.dto.response.NotificationResponseDTO;
import campus_nexus.entity.Notification;
import campus_nexus.entity.User;
import campus_nexus.enums.NotificationType;
import campus_nexus.repository.NotificationRepository;
import campus_nexus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    public NotificationResponseDTO createNotification(Long userId, NotificationType type, String message, String referenceId) {
        logger.info("Creating notification for user ID: {}, type: {}", userId, type);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        String finalMessage = (message != null && !message.isBlank())
                ? message
                : type.getDefaultMessage();

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(finalMessage);
        notification.setReferenceId(referenceId);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);
        logger.debug("Notification created with ID: {}", saved.getId());

        return convertToResponseDTO(saved);
    }

    public int createBulkNotifications(List<Long> userIds, NotificationType type, String message, String referenceId) {
        logger.info("Creating bulk notifications for {} users, type: {}", userIds.size(), type);

        int createdCount = 0;
        for (Long userId : userIds) {
            try {
                createNotification(userId, type, message, referenceId);
                createdCount++;
            } catch (Exception e) {
                logger.warn("Failed to create notification for user {}: {}", userId, e.getMessage());
            }
        }

        logger.info("Created {} out of {} notifications", createdCount, userIds.size());
        return createdCount;
    }

    public Page<NotificationResponseDTO> getUserNotifications(Long userId, Pageable pageable) {
        logger.debug("Fetching notifications for user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        Page<Notification> notificationPage = notificationRepository.findByUserId(userId, pageable);
        return notificationPage.map(this::convertToResponseDTO);
    }

    public Page<NotificationResponseDTO> getUnreadNotifications(Long userId, Pageable pageable) {
        logger.debug("Fetching unread notifications for user ID: {}", userId);

        Page<Notification> notificationPage = notificationRepository.findByUserIdAndIsRead(userId, false, pageable);
        return notificationPage.map(this::convertToResponseDTO);
    }

    public long getUnreadCount(Long userId) {
        logger.debug("Getting unread count for user ID: {}", userId);
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    public void markAsRead(Long notificationId, Long userId) {
        logger.info("Marking notification ID: {} as read for user ID: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));

        if (notification.getUser() == null || !notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to this user");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);

        auditLogService.log("MARK_NOTIFICATION_READ", "user_" + userId, "Notification ID: " + notificationId);
    }

    public void markAllAsRead(Long userId) {
        logger.info("Marking all notifications as read for user ID: {}", userId);

        List<Notification> notifications = notificationRepository.findByUserIdAndIsRead(userId, false);
        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);

        auditLogService.log("MARK_ALL_NOTIFICATIONS_READ", "user_" + userId,
                "Marked " + notifications.size() + " notifications as read");
    }

    public void deleteNotification(Long notificationId, Long userId) {
        logger.info("Deleting notification ID: {} for user ID: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));

        if (notification.getUser() == null || !notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to this user");
        }

        notificationRepository.delete(notification);
        auditLogService.log("DELETE_NOTIFICATION", "user_" + userId, "Deleted notification ID: " + notificationId);
    }

    public void deleteAllNotifications(Long userId) {
        logger.info("Deleting all notifications for user ID: {}", userId);

        List<Notification> notifications = notificationRepository.findByUserId(userId, Pageable.unpaged()).getContent();
        notificationRepository.deleteByUserId(userId);

        auditLogService.log("DELETE_ALL_NOTIFICATIONS", "user_" + userId,
                "Deleted " + notifications.size() + " notifications");
    }

    public void notifyBookingStatusChange(Long userId, String bookingId, String status, String reason) {
        NotificationType type;
        String message;

        switch (status.toUpperCase()) {
            case "APPROVED":
                type = NotificationType.BOOKING_APPROVED;
                message = "Your booking #" + bookingId + " has been approved!";
                break;
            case "REJECTED":
                type = NotificationType.BOOKING_REJECTED;
                message = "Your booking #" + bookingId + " has been rejected. Reason: "
                        + (reason != null ? reason : "Not specified");
                break;
            case "CANCELLED":
                type = NotificationType.BOOKING_CANCELLED;
                message = "Your booking #" + bookingId + " has been cancelled.";
                break;
            default:
                type = NotificationType.BOOKING_PENDING;
                message = "Your booking #" + bookingId + " has been submitted and is pending approval.";
        }

        createNotification(userId, type, message, "BOOKING_" + bookingId);
    }

    public void notifyTicketStatusChange(Long userId, String ticketId, String status) {
        NotificationType type;
        String message;

        switch (status.toUpperCase()) {
            case "ASSIGNED":
                type = NotificationType.TICKET_ASSIGNED;
                message = "A technician has been assigned to your ticket #" + ticketId;
                break;
            case "RESOLVED":
                type = NotificationType.TICKET_RESOLVED;
                message = "Your ticket #" + ticketId + " has been resolved!";
                break;
            case "REJECTED":
                type = NotificationType.TICKET_REJECTED;
                message = "Your ticket #" + ticketId + " has been rejected.";
                break;
            case "CLOSED":
                type = NotificationType.TICKET_CLOSED;
                message = "Your ticket #" + ticketId + " has been closed.";
                break;
            default:
                type = NotificationType.TICKET_STATUS_UPDATED;
                message = "Your ticket #" + ticketId + " status has been updated to " + status;
        }

        createNotification(userId, type, message, "TICKET_" + ticketId);
    }

    public void notifyNewComment(Long ticketOwnerId, String ticketId, String commenterName) {
        String message = commenterName + " added a comment to your ticket #" + ticketId;
        createNotification(ticketOwnerId, NotificationType.COMMENT_ADDED, message, "TICKET_" + ticketId);
    }

    private NotificationResponseDTO convertToResponseDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .userId(notification.getUser() != null ? notification.getUser().getId() : null)
                .userEmail(notification.getUser() != null ? notification.getUser().getEmail() : null)
                .userName(notification.getUser() != null ? notification.getUser().getName() : null)
                .type(notification.getType())
                .typeTitle(notification.getType() != null ? notification.getType().getTitle() : null)
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
