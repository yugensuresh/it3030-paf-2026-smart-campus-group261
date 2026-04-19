package campus_nexus.repository;

import campus_nexus.entity.Notification;
import campus_nexus.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Notification entity.
 * Provides CRUD operations and custom query methods for notification management.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ==================== Basic Find Methods ====================

    /**
     * Get all notifications for a specific user with pagination
     * @param userId User ID
     * @param pageable Pagination info
     * @return Page of notifications
     */
    Page<Notification> findByUserId(Long userId, Pageable pageable);

    /**
     * Get unread notifications for a specific user
     * @param userId User ID
     * @param isRead false for unread
     * @param pageable Pagination info
     * @return Page of unread notifications
     */
    Page<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead, Pageable pageable);

    /**
     * Get all unread notifications for a user (no pagination)
     * @param userId User ID
     * @return List of unread notifications
     */
    List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead);

    // ==================== Count Methods ====================

    /**
     * Count unread notifications for a user (for notification badge)
     * @param userId User ID
     * @return Count of unread notifications
     */
    long countByUserIdAndIsRead(Long userId, Boolean isRead);

    /**
     * Count notifications by type for a user
     * @param userId User ID
     * @param type Notification type
     * @return Count
     */
    long countByUserIdAndType(Long userId, NotificationType type);

    // ==================== Type-Based Queries ====================

    /**
     * Get notifications by type for a user
     * @param userId User ID
     * @param type Notification type
     * @param pageable Pagination info
     * @return Page of notifications
     */
    Page<Notification> findByUserIdAndType(Long userId, NotificationType type, Pageable pageable);

    /**
     * Get notifications by reference ID (e.g., all notifications for a specific booking)
     * @param referenceId Reference ID (e.g., "BOOKING_123")
     * @return List of notifications
     */
    List<Notification> findByReferenceId(String referenceId);

    // ==================== Date-Based Queries ====================

    /**
     * Get notifications created after a specific date
     * @param userId User ID
     * @param dateTime Date threshold
     * @return List of recent notifications
     */
    List<Notification> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime dateTime);

    // ==================== Update Methods ====================

    /**
     * Mark all unread notifications as read for a user
     * @param userId User ID
     * @return Number of updated notifications
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * Mark specific notification as read
     * @param id Notification ID
     * @param userId User ID (for security)
     * @return Number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id AND n.user.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * Delete old notifications (older than specified date)
     * @param dateThreshold Date threshold
     * @return Number of deleted records
     */
    @Modifying
    @Transactional
    long deleteByCreatedAtBefore(LocalDateTime dateThreshold);

    /**
     * Delete all notifications for a user
     * @param userId User ID
     */
    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
}