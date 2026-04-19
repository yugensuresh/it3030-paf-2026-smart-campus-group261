package campus_nexus.controller;

import campus_nexus.dto.request.TicketRequestDTO;
import campus_nexus.dto.response.TicketResponseDTO;
import campus_nexus.enums.PriorityLevel;
import campus_nexus.enums.TicketStatus;
import campus_nexus.service.MaintenanceTicketService;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing Maintenance Tickets.
 * Provides endpoints for reporting issues with 3 images,
 * technician assignment, and ticket workflow management.
 *
 * Workflow: OPEN → IN_PROGRESS → RESOLVED → CLOSED
 */
@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class MaintenanceTicketController {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceTicketController.class);

    @Autowired
    private MaintenanceTicketService ticketService;

    /**
     * POST /api/tickets - Create a new maintenance ticket
     * Supports priority level and up to 3 image references
     */
    @PostMapping
    public ResponseEntity<TicketResponseDTO> createTicket(@Valid @RequestBody TicketRequestDTO request) {
        logger.info("POST /api/tickets - Creating new ticket for resource: {}", request.getResourceId());
        TicketResponseDTO created = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/tickets - Get all tickets with pagination (Admin only)
     * Query params: page, size, sort, direction
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        logger.info("GET /api/tickets - page: {}, size: {}, sort: {}, direction: {}", page, size, sort, direction);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<TicketResponseDTO> ticketPage = ticketService.getAllTickets(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", ticketPage.getContent());
        response.put("currentPage", ticketPage.getNumber());
        response.put("totalItems", ticketPage.getTotalElements());
        response.put("totalPages", ticketPage.getTotalPages());
        response.put("pageSize", ticketPage.getSize());
        response.put("hasNext", ticketPage.hasNext());
        response.put("hasPrevious", ticketPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/tickets/{id} - Get single ticket by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable Long id) {
        logger.info("GET /api/tickets/{} - Fetching ticket", id);
        TicketResponseDTO ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    /**
     * GET /api/tickets/user/{userId} - Get tickets for specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TicketResponseDTO>> getTicketsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("GET /api/tickets/user/{} - page: {}, size: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TicketResponseDTO> tickets = ticketService.getTicketsByUser(userId, pageable);
        return ResponseEntity.ok(tickets);
    }

    /**
     * GET /api/tickets/status/{status} - Get tickets by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TicketResponseDTO>> getTicketsByStatus(
            @PathVariable TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("GET /api/tickets/status/{} - page: {}, size: {}", status, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TicketResponseDTO> tickets = ticketService.getTicketsByStatus(status, pageable);
        return ResponseEntity.ok(tickets);
    }

    /**
     * PATCH /api/tickets/{id}/status - Update ticket status
     * Request body: { "status": "IN_PROGRESS" }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponseDTO> updateTicketStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate,
            @RequestHeader(value = "X-User-Email", defaultValue = "system@campus.com") String userEmail,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String role) {

        String statusStr = statusUpdate.get("status");
        logger.info("PATCH /api/tickets/{}/status - New status: {}, user: {}, role: {}", id, statusStr, userEmail, role);

        TicketStatus status;
        try {
            status = TicketStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value. Allowed: OPEN, IN_PROGRESS, RESOLVED, CLOSED, REJECTED");
        }

        TicketResponseDTO updated = ticketService.updateTicketStatus(id, status, userEmail, role);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /api/tickets/{id}/assign - Assign technician to ticket (Admin only)
     * Request body: { "technicianId": 5 }
     */
    @PatchMapping("/{id}/assign")
    public ResponseEntity<TicketResponseDTO> assignTechnician(
            @PathVariable Long id,
            @RequestBody Map<String, Long> assignRequest,
            @RequestHeader(value = "X-User-Email", defaultValue = "admin@campus.com") String adminEmail) {

        Long technicianId = assignRequest.get("technicianId");
        logger.info("PATCH /api/tickets/{}/assign - Technician ID: {}, admin: {}", id, technicianId, adminEmail);

        TicketResponseDTO updated = ticketService.assignTechnician(id, technicianId, adminEmail);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /api/tickets/{id}/notes - Add resolution notes (Technician action)
     * Request body: { "resolutionNotes": "Fixed the projector bulb" }
     */
    @PatchMapping("/{id}/notes")
    public ResponseEntity<TicketResponseDTO> addResolutionNotes(
            @PathVariable Long id,
            @RequestBody Map<String, String> notesRequest,
            @RequestHeader(value = "X-User-Email", defaultValue = "technician@campus.com") String technicianEmail) {

        String resolutionNotes = notesRequest.get("resolutionNotes");
        logger.info("PATCH /api/tickets/{}/notes - Adding resolution notes by: {}", id, technicianEmail);

        TicketResponseDTO updated = ticketService.addResolutionNotes(id, resolutionNotes, technicianEmail);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /api/tickets/{id}/reject - Reject ticket with reason (Admin only)
     * Request body: { "rejectionReason": "Duplicate report" }
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<TicketResponseDTO> rejectTicket(
            @PathVariable Long id,
            @RequestBody Map<String, String> rejectRequest,
            @RequestHeader(value = "X-User-Email", defaultValue = "admin@campus.com") String adminEmail) {

        String rejectionReason = rejectRequest.get("rejectionReason");
        logger.info("PATCH /api/tickets/{}/reject - Reason: {}, admin: {}", id, rejectionReason, adminEmail);

        TicketResponseDTO updated = ticketService.rejectTicket(id, rejectionReason, adminEmail);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/tickets/{id} - Delete a ticket
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Email", defaultValue = "admin@campus.com") String userEmail,
            @RequestHeader(value = "X-User-Role", defaultValue = "ADMIN") String role) {

        logger.info("DELETE /api/tickets/{} - Deleting ticket by: {} (role: {})", id, userEmail, role);
        ticketService.deleteTicket(id, userEmail, role);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/tickets/search - Advanced search with filters (Admin only)
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchTickets(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) Long technicianId,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) PriorityLevel priority,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("GET /api/tickets/search - userId: {}, resourceId: {}, status: {}, priority: {}", userId, resourceId, status, priority);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TicketResponseDTO> ticketPage = ticketService.searchTickets(
                userId, resourceId, technicianId, status, priority, category, search, startDate, endDate, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", ticketPage.getContent());
        response.put("currentPage", ticketPage.getNumber());
        response.put("totalItems", ticketPage.getTotalElements());
        response.put("totalPages", ticketPage.getTotalPages());
        response.put("pageSize", ticketPage.getSize());
        response.put("hasNext", ticketPage.hasNext());
        response.put("hasPrevious", ticketPage.hasPrevious());

        return ResponseEntity.ok(response);
    }
}