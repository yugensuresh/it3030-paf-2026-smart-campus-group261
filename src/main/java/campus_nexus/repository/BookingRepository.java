package campus_nexus.repository;

import campus_nexus.entity.Booking;
import campus_nexus.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Check for conflicting bookings on the same resource, date, and overlapping time range
     * Excludes bookings with CANCELLED or REJECTED status (they don't block)
     *
     * @param resourceId ID of the resource
     * @param date Date of the booking
     * @param startTime Start time of the requested booking
     * @param endTime End time of the requested booking
     * @return List of conflicting bookings
     */
    @Query("SELECT b FROM Booking b WHERE b.resource.id = :resourceId " +
            "AND b.bookingDate = :date " +
            "AND b.status NOT IN ('CANCELLED', 'REJECTED') " +
            "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findConflictingBookings(
            @Param("resourceId") Long resourceId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    /**
     * Get all bookings for a specific user with pagination
     * @param userId User ID
     * @param pageable Pagination info
     * @return Page of bookings
     */
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    /**
     * Get bookings by status with pagination (for Admin filtering)
     * @param status Booking status (PENDING, APPROVED, REJECTED, CANCELLED)
     * @param pageable Pagination info
     * @return Page of bookings
     */
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    /**
     * Get bookings for a specific resource with pagination
     * @param resourceId Resource ID
     * @param pageable Pagination info
     * @return Page of bookings
     */
    Page<Booking> findByResourceId(Long resourceId, Pageable pageable);

    /**
     * Search bookings with multiple filters (for Admin dashboard)
     * @param userId Filter by user ID (optional)
     * @param resourceId Filter by resource ID (optional)
     * @param status Filter by status (optional)
     * @param startDate Filter by date from (optional)
     * @param endDate Filter by date to (optional)
     * @param pageable Pagination info
     * @return Page of bookings
     */
    @Query("SELECT b FROM Booking b WHERE " +
            "(:userId IS NULL OR b.user.id = :userId) AND " +
            "(:resourceId IS NULL OR b.resource.id = :resourceId) AND " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:startDate IS NULL OR b.bookingDate >= :startDate) AND " +
            "(:endDate IS NULL OR b.bookingDate <= :endDate)")
    Page<Booking> searchBookings(
            @Param("userId") Long userId,
            @Param("resourceId") Long resourceId,
            @Param("status") BookingStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}