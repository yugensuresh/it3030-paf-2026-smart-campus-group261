package campus_nexus.service;

import campus_nexus.dto.request.BookingRequestDTO;
import campus_nexus.dto.response.BookingResponseDTO;
import campus_nexus.entity.Booking;
import campus_nexus.entity.Resource;
import campus_nexus.entity.User;
import campus_nexus.enums.BookingStatus;
import campus_nexus.enums.NotificationType;
import campus_nexus.repository.BookingRepository;
import campus_nexus.repository.ResourceRepository;
import campus_nexus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * BookingService handles the business logic for room/equipment reservations
 * Implements industry-standard workflow: PENDING -> APPROVED/REJECTED/CANCELLED
 */
@Service
@Transactional(readOnly = true)
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Get all bookings with pagination (Admin only)
     * @param pageable Pagination and sorting info
     * @return Page of booking response DTOs
     */
    public Page<BookingResponseDTO> getAllBookings(Pageable pageable) {
        logger.debug("Fetching all bookings with pagination");
        Page<Booking> bookingPage = bookingRepository.findAll(pageable);
        return bookingPage.map(this::convertToResponseDTO);
    }

    /**
     * Get bookings for a specific user
     * @param userId User ID
     * @param pageable Pagination info
     * @return Page of booking response DTOs
     */
    public Page<BookingResponseDTO> getBookingsByUser(Long userId, Pageable pageable) {
        logger.debug("Fetching bookings for user ID: {}", userId);

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        Page<Booking> bookingPage = bookingRepository.findByUserId(userId, pageable);
        return bookingPage.map(this::convertToResponseDTO);
    }

    /**
     * Get booking by ID
     * @param id Booking ID
     * @return Booking response DTO
     */
    public BookingResponseDTO getBookingById(Long id) {
        logger.debug("Fetching booking by ID: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
        return convertToResponseDTO(booking);
    }

    /**
     * Create a new booking request
     * Initial status is set to PENDING and conflicts are checked before saving
     * @param request Booking request DTO
     * @return Saved booking response DTO with PENDING status
     */
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        logger.info("Creating new booking for resource ID: {}, user ID: {}", request.getResourceId(), request.getUserId());

        // 1. Validate time range (start time must be before end time)
        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new RuntimeException("Invalid time range: Start time must be before end time");
        }

        // 2. Validate time slot is within reasonable hours (8 AM to 10 PM)
        if (request.getStartTime().isBefore(LocalTime.of(8, 0)) || request.getEndTime().isAfter(LocalTime.of(22, 0))) {
            throw new RuntimeException("Bookings are only allowed between 8:00 AM and 10:00 PM");
        }

        // 3. Validate entities exist
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + request.getResourceId()));

        // 4. Check if resource is ACTIVE (using getStatus() method)
        if (resource.getStatus() == null || !"ACTIVE".equals(resource.getStatus())) {
            String currentStatus = resource.getStatus() != null ? resource.getStatus() : "UNKNOWN";
            throw new RuntimeException("Resource is currently " + currentStatus + " and cannot be booked");
        }

        // 5. Conflict Prevention Logic
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                request.getResourceId(),
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            logger.warn("Conflict detected for resource ID: {} on date: {}", request.getResourceId(), request.getBookingDate());
            throw new RuntimeException("Time slot conflict detected! This resource is already booked for the selected period.");
        }

        // 6. Create and save booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setResource(resource);
        booking.setBookingDate(request.getBookingDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setPurpose(request.getPurpose());
        booking.setStatus(BookingStatus.PENDING);
        booking.setRejectionReason(null);

        Booking savedBooking = bookingRepository.save(booking);
        logger.info("Booking created successfully with ID: {}", savedBooking.getId());

        // 7. Audit Log
        auditLogService.log(
                "CREATE_BOOKING",
                user.getEmail(),
                "Booking ID " + savedBooking.getId() + " created for " + resource.getName() + " on " + request.getBookingDate()
        );

        return convertToResponseDTO(savedBooking);
    }

    /**
     * Admin action: Approve or Reject a booking request with a reason
     * @param id Booking ID to update
     * @param status The new status (APPROVED/REJECTED)
     * @param reason Optional reason (required for REJECTED)
     * @param adminEmail Email of admin performing the action
     * @return Updated booking response DTO
     */
    @Transactional
    public BookingResponseDTO updateBookingStatus(Long id, BookingStatus status, String reason, String adminEmail) {
        logger.info("Updating booking ID: {} to status: {} by admin: {}", id, status, adminEmail);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        // Cannot modify already CANCELLED bookings
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Cannot modify a cancelled booking");
        }

        // If rejecting, reason is required
        if (status == BookingStatus.REJECTED && (reason == null || reason.trim().isEmpty())) {
            throw new RuntimeException("Rejection reason is required when rejecting a booking");
        }

        booking.setStatus(status);
        booking.setRejectionReason(reason);

        Booking updatedBooking = bookingRepository.save(booking);
        logger.info("Booking ID: {} status updated to: {}", id, status);

        // Audit Log for Admin Action
        auditLogService.log(
                "UPDATE_BOOKING_STATUS",
                adminEmail,
                "Booking ID " + id + " status changed to " + status
        );

        notifyBookingUserOfDecision(updatedBooking, status, reason);

        return convertToResponseDTO(updatedBooking);
    }

    private void notifyBookingUserOfDecision(Booking booking, BookingStatus status, String reason) {
        Long userId = booking.getUser().getId();
        String refId = String.valueOf(booking.getId());
        String resourceName = booking.getResource().getName();
        try {
            if (status == BookingStatus.APPROVED) {
                String message = String.format(
                        "Your booking for \"%s\" on %s from %s to %s was approved.",
                        resourceName,
                        booking.getBookingDate(),
                        booking.getStartTime(),
                        booking.getEndTime());
                notificationService.createNotification(userId, NotificationType.BOOKING_APPROVED, message, refId);
            } else if (status == BookingStatus.REJECTED) {
                String message = String.format(
                        "Your booking for \"%s\" on %s was rejected. Reason: %s",
                        resourceName,
                        booking.getBookingDate(),
                        reason != null ? reason.trim() : "");
                notificationService.createNotification(userId, NotificationType.BOOKING_REJECTED, message, refId);
            }
        } catch (Exception e) {
            logger.warn("Could not notify user {} about booking {} status {}: {}", userId, booking.getId(), status, e.getMessage());
        }
    }

    /**
     * User action: Cancel their own booking (only if status is PENDING or APPROVED)
     * @param id Booking ID
     * @param userEmail Email of user cancelling (for audit)
     */
    @Transactional
    public void cancelBooking(Long id, String userEmail) {
        logger.info("Cancelling booking ID: {} by user: {}", id, userEmail);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        if (userEmail == null || userEmail.isBlank()) {
            throw new RuntimeException("User email is required to cancel a booking");
        }
        if (!booking.getUser().getEmail().equalsIgnoreCase(userEmail.trim())) {
            throw new RuntimeException("You can only cancel your own bookings");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }
        if (booking.getStatus() == BookingStatus.REJECTED) {
            throw new RuntimeException("Cannot cancel a rejected booking");
        }
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.APPROVED) {
            throw new RuntimeException("Only pending or approved bookings can be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Audit Log
        auditLogService.log(
                "CANCEL_BOOKING",
                userEmail,
                "Booking ID " + id + " was cancelled by user"
        );

        logger.info("Booking ID: {} cancelled successfully", id);
    }

    /**
     * Remove all APPROVED, REJECTED, and CANCELLED bookings for a user. PENDING requests are kept.
     */
    @Transactional
    public int deleteUserBookingHistoryExceptPending(Long userId, String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new RuntimeException("User email is required");
        }
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        if (!owner.getEmail().equalsIgnoreCase(userEmail.trim())) {
            throw new RuntimeException("You can only clear your own bookings");
        }
        List<BookingStatus> removable = Arrays.asList(
                BookingStatus.APPROVED,
                BookingStatus.REJECTED,
                BookingStatus.CANCELLED);
        int removed = bookingRepository.deleteByUserIdAndStatusIn(userId, removable);
        if (removed > 0) {
            auditLogService.log(
                    "CLEAR_BOOKING_HISTORY",
                    userEmail,
                    "Deleted " + removed + " booking(s) in statuses APPROVED/REJECTED/CANCELLED for user ID " + userId);
        }
        logger.info("Cleared {} non-pending bookings for user {}", removed, userId);
        return removed;
    }

    /**
     * Search bookings with multiple filters (Admin dashboard)
     * @param userId Filter by user ID
     * @param resourceId Filter by resource ID
     * @param status Filter by status
     * @param startDate Filter by date from
     * @param endDate Filter by date to
     * @param pageable Pagination info
     * @return Page of booking response DTOs
     */
    public Page<BookingResponseDTO> searchBookings(
            Long userId,
            Long resourceId,
            BookingStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        logger.debug("Searching bookings with filters - userId: {}, resourceId: {}, status: {}, startDate: {}, endDate: {}",
                userId, resourceId, status, startDate, endDate);

        Page<Booking> bookingPage = bookingRepository.searchBookings(
                userId, resourceId, status, startDate, endDate, pageable);

        return bookingPage.map(this::convertToResponseDTO);
    }

    /**
     * Convert Booking entity to BookingResponseDTO
     * @param booking Booking entity
     * @return BookingResponseDTO
     */
    private BookingResponseDTO convertToResponseDTO(Booking booking) {
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .userEmail(booking.getUser().getEmail())
                .userName(booking.getUser().getName())
                .resourceId(booking.getResource().getId())
                .resourceName(booking.getResource().getName())
                .resourceType(booking.getResource().getType() != null ? booking.getResource().getType().toString() : "UNKNOWN")
                .resourceLocation(booking.getResource().getLocation())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .purpose(booking.getPurpose())
                .status(booking.getStatus())
                .rejectionReason(booking.getRejectionReason())
                .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
}