package campus_nexus.controller;

import campus_nexus.dto.request.BookingRequestDTO;
import campus_nexus.dto.response.BookingResponseDTO;
import campus_nexus.enums.BookingStatus;
import campus_nexus.service.BookingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    /**
     * GET /api/bookings - Get all bookings with pagination (Admin only)
     * Query params:
     * - page: page number (default 0)
     * - size: page size (default 10)
     * - sort: sort field (default id)
     * - direction: asc/desc (default desc)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        logger.info("GET /api/bookings - page: {}, size: {}, sort: {}, direction: {}", page, size, sort, direction);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<BookingResponseDTO> bookingPage = bookingService.getAllBookings(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", bookingPage.getContent());
        response.put("currentPage", bookingPage.getNumber());
        response.put("totalItems", bookingPage.getTotalElements());
        response.put("totalPages", bookingPage.getTotalPages());
        response.put("pageSize", bookingPage.getSize());
        response.put("hasNext", bookingPage.hasNext());
        response.put("hasPrevious", bookingPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/bookings/user/{userId} - Get bookings for specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<BookingResponseDTO>> getBookingsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("GET /api/bookings/user/{} - page: {}, size: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookingDate"));
        Page<BookingResponseDTO> bookings = bookingService.getBookingsByUser(userId, pageable);
        return ResponseEntity.ok(bookings);
    }

    /**
     * GET /api/bookings/{id} - Get single booking by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Long id) {
        logger.info("GET /api/bookings/{} - Fetching booking", id);
        BookingResponseDTO booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    /**
     * POST /api/bookings - Create new booking request
     */
    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO request) {
        logger.info("POST /api/bookings - Creating booking for resource: {}", request.getResourceId());
        BookingResponseDTO created = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PATCH /api/bookings/{id}/status - Admin approve/reject booking
     * Request body: { "status": "APPROVED", "reason": "optional reason" }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingResponseDTO> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate,
            @RequestHeader(value = "X-User-Email", defaultValue = "admin@campus.com") String adminEmail) {

        String statusStr = statusUpdate.get("status");
        String reason = statusUpdate.get("reason");

        logger.info("PATCH /api/bookings/{}/status - New status: {}, admin: {}", id, statusStr, adminEmail);

        BookingStatus status;
        try {
            status = BookingStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value. Allowed: APPROVED, REJECTED");
        }

        // Only APPROVED and REJECTED are allowed via this endpoint
        if (status != BookingStatus.APPROVED && status != BookingStatus.REJECTED) {
            throw new RuntimeException("Status can only be updated to APPROVED or REJECTED");
        }

        BookingResponseDTO updated = bookingService.updateBookingStatus(id, status, reason, adminEmail);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/bookings/{id} - Cancel booking (User action)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", defaultValue = "user@campus.com") String userEmail) {

        logger.info("DELETE /api/bookings/{} - Cancelling booking by: {}", id, userEmail);
        bookingService.cancelBooking(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/bookings/search - Advanced search with filters (Admin only)
     * Query params:
     * - userId (optional)
     * - resourceId (optional)
     * - status (optional: PENDING, APPROVED, REJECTED, CANCELLED)
     * - startDate (optional: yyyy-MM-dd)
     * - endDate (optional: yyyy-MM-dd)
     * - page (default 0)
     * - size (default 10)
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("GET /api/bookings/search - userId: {}, resourceId: {}, status: {}, startDate: {}, endDate: {}",
                userId, resourceId, status, startDate, endDate);

        BookingStatus bookingStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status value. Allowed: PENDING, APPROVED, REJECTED, CANCELLED");
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookingDate"));
        Page<BookingResponseDTO> bookingPage = bookingService.searchBookings(
                userId, resourceId, bookingStatus, startDate, endDate, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", bookingPage.getContent());
        response.put("currentPage", bookingPage.getNumber());
        response.put("totalItems", bookingPage.getTotalElements());
        response.put("totalPages", bookingPage.getTotalPages());
        response.put("pageSize", bookingPage.getSize());
        response.put("hasNext", bookingPage.hasNext());
        response.put("hasPrevious", bookingPage.hasPrevious());

        return ResponseEntity.ok(response);
    }
}