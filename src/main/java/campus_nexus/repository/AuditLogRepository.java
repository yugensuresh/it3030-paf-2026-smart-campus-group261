package campus_nexus.repository;

import campus_nexus.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditLog entity.
 * Provides CRUD operations and advanced query methods for system activity logs.
 * Supports pagination, filtering, and date range searches for admin dashboard.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs by user who performed the action
     * Used for "User Activity History" feature
     *
     * @param performedBy User email or identifier
     * @param pageable Pagination and sorting info
     * @return Page of audit logs for the specified user
     */
    Page<AuditLog> findByPerformedBy(String performedBy, Pageable pageable);

    /**
     * Find audit logs by action type
     * Used for filtering specific operations (e.g., all CREATE_BOOKING actions)
     *
     * @param action Action type (CREATE_BOOKING, UPDATE_STATUS, DELETE_RESOURCE, etc.)
     * @param pageable Pagination and sorting info
     * @return Page of audit logs for the specified action
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * Find audit logs by action and user (combined filter)
     *
     * @param action Action type
     * @param performedBy User email or identifier
     * @param pageable Pagination and sorting info
     * @return Page of audit logs matching both criteria
     */
    Page<AuditLog> findByActionAndPerformedBy(String action, String performedBy, Pageable pageable);

    /**
     * Find audit logs within a date range
     * Used for "Audit Report Generation" feature
     *
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param pageable Pagination and sorting info
     * @return Page of audit logs within the date range
     */
    Page<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find audit logs by action, user, and date range (all filters combined)
     * Used for advanced admin search dashboard
     *
     * @param action Action type (can be null)
     * @param performedBy User email (can be null)
     * @param startDate Start date (can be null)
     * @param endDate End date (can be null)
     * @param pageable Pagination and sorting info
     * @return Page of audit logs matching all provided criteria
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:performedBy IS NULL OR a.performedBy = :performedBy) AND " +
            "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR a.timestamp <= :endDate)")
    Page<AuditLog> searchAuditLogs(
            @Param("action") String action,
            @Param("performedBy") String performedBy,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Get recent audit logs (last 24 hours)
     * Used for dashboard "Recent Activity" widget
     *
     * @param last24Hours The timestamp from 24 hours ago
     * @return List of recent audit logs
     */
    List<AuditLog> findByTimestampAfter(LocalDateTime last24Hours);

    /**
     * Count logs by action type
     * Used for analytics dashboard charts
     *
     * @return List of object arrays containing action and count
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> countLogsByAction();

    /**
     * Count logs by user (top activity users)
     * Used for "Most Active Users" report
     *
     * @return List of object arrays containing user email and activity count
     */
    @Query("SELECT a.performedBy, COUNT(a) FROM AuditLog a GROUP BY a.performedBy ORDER BY COUNT(a) DESC")
    List<Object[]> countLogsByUser();

    /**
     * Delete audit logs older than specified date
     * Used for data retention policy (optional cleanup job)
     *
     * @param dateThreshold Date threshold (delete logs before this date)
     * @return Number of deleted records
     */
    long deleteByTimestampBefore(LocalDateTime dateThreshold);
}