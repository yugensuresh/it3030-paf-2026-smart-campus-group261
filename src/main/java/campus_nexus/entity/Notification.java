package campus_nexus.entity;

import campus_nexus.enums.NotificationType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Notification entity for in-app notifications.
 * Users receive notifications for:
 * - Booking approval/rejection
 * - Ticket status changes
 * - New comments on their tickets
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
@CompoundIndexes({
        @CompoundIndex(name = "notification_user_idx", def = "{'user.id': 1}"),
        @CompoundIndex(name = "notification_read_idx", def = "{'isRead': 1}"),
        @CompoundIndex(name = "notification_created_idx", def = "{'createdAt': -1}")
})
public class Notification {

    @Id
    private Long id;

    private User user;  // Who receives this notification

    private NotificationType type;  // BOOKING_APPROVED, TICKET_UPDATED, etc.

    private String message;  // Notification content

    private String details;  // Additional JSON data or reference link

    private String referenceId;  // e.g., Booking ID: 123, Ticket ID: 456

    private Boolean isRead = false;  // Read status

    private LocalDateTime createdAt;

    public void onCreate() {
        if (isRead == null) {
            isRead = false;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * Check if notification is unread
     */
    public boolean isUnread() {
        return !isRead;
    }
}
