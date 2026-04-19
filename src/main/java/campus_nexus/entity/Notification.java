package campus_nexus.entity;

import campus_nexus.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Notification entity for in-app notifications.
 * Users receive notifications for:
 * - Booking approval/rejection
 * - Ticket status changes
 * - New comments on their tickets
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_is_read", columnList = "is_read"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Who receives this notification

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;  // BOOKING_APPROVED, TICKET_UPDATED, etc.

    @Column(nullable = false, length = 2000)
    private String message;  // Notification content

    @Column(length = 500)
    private String details;  // Additional JSON data or reference link

    @Column(nullable = false)
    private String referenceId;  // e.g., Booking ID: 123, Ticket ID: 456

    @Column(nullable = false)
    private Boolean isRead = false;  // Read status

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (isRead == null) {
            isRead = false;
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