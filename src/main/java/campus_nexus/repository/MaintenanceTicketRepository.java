package campus_nexus.repository;

import campus_nexus.entity.MaintenanceTicket;
import campus_nexus.enums.PriorityLevel;
import campus_nexus.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for MaintenanceTicket entity.
 * Provides CRUD operations and advanced query methods for ticket management.
 * Supports filtering by status, priority, user, resource, and date ranges.
 */
@Repository
public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicket, Long> {

    // ==================== Basic Find Methods ====================

    Optional<MaintenanceTicket> findById(Long id);

    Page<MaintenanceTicket> findAll(Pageable pageable);

    // ==================== Status-Based Queries ====================

    Page<MaintenanceTicket> findByStatus(TicketStatus status, Pageable pageable);

    List<MaintenanceTicket> findByStatus(TicketStatus status);

    long countByStatus(TicketStatus status);

    // ==================== Priority-Based Queries ====================

    Page<MaintenanceTicket> findByPriority(PriorityLevel priority, Pageable pageable);

    List<MaintenanceTicket> findByPriorityOrderByCreatedAtAsc(PriorityLevel priority);

    // ==================== User-Based Queries ====================

    Page<MaintenanceTicket> findByUserId(Long userId, Pageable pageable);

    List<MaintenanceTicket> findByUserId(Long userId);

    // ==================== Technician-Based Queries ====================

    Page<MaintenanceTicket> findByTechnicianId(Long technicianId, Pageable pageable);

    List<MaintenanceTicket> findByTechnicianIdAndStatus(Long technicianId, TicketStatus status);

    // ==================== Resource-Based Queries ====================

    Page<MaintenanceTicket> findByResourceId(Long resourceId, Pageable pageable);

    List<MaintenanceTicket> findByResourceId(Long resourceId);

    // ==================== Date Range Queries ====================

    List<MaintenanceTicket> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<MaintenanceTicket> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // ==================== Advanced Search with Filters ====================

    /**
     * Advanced search with multiple filters (for admin dashboard)
     */
    @Query("SELECT t FROM MaintenanceTicket t WHERE " +
            "(:userId IS NULL OR t.user.id = :userId) AND " +
            "(:resourceId IS NULL OR t.resource.id = :resourceId) AND " +
            "(:technicianId IS NULL OR t.technician.id = :technicianId) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:category IS NULL OR LOWER(t.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
            "(:search IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR t.createdAt <= :endDate)")
    Page<MaintenanceTicket> searchTickets(
            @Param("userId") Long userId,
            @Param("resourceId") Long resourceId,
            @Param("technicianId") Long technicianId,
            @Param("status") TicketStatus status,
            @Param("priority") PriorityLevel priority,
            @Param("category") String category,
            @Param("search") String search,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // ==================== Dashboard Statistics Queries ====================

    /**
     * Count tickets by status (for dashboard charts)
     */
    @Query("SELECT t.status, COUNT(t) FROM MaintenanceTicket t GROUP BY t.status")
    List<Object[]> countTicketsByStatus();

    /**
     * Count tickets by priority (for dashboard charts)
     */
    @Query("SELECT t.priority, COUNT(t) FROM MaintenanceTicket t GROUP BY t.priority")
    List<Object[]> countTicketsByPriority();

    /**
     * Get tickets assigned to a specific technician that are still open or in progress
     */
    @Query("SELECT t FROM MaintenanceTicket t WHERE t.technician.id = :technicianId AND t.status IN ('OPEN', 'IN_PROGRESS')")
    List<MaintenanceTicket> findActiveTicketsByTechnician(@Param("technicianId") Long technicianId);

    /**
     * Get high priority tickets that are still open (for urgent dashboard)
     */
    @Query("SELECT t FROM MaintenanceTicket t WHERE t.priority = 'HIGH' AND t.status IN ('OPEN', 'IN_PROGRESS') ORDER BY t.createdAt ASC")
    List<MaintenanceTicket> findUrgentHighPriorityTickets();
}