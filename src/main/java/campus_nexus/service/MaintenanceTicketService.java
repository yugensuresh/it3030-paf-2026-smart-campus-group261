package campus_nexus.service;

import campus_nexus.config.MongoDocumentPreparer;
import campus_nexus.dto.request.TicketRequestDTO;
import campus_nexus.dto.response.TicketResponseDTO;
import campus_nexus.entity.MaintenanceTicket;
import campus_nexus.entity.Resource;
import campus_nexus.entity.User;
import campus_nexus.enums.PriorityLevel;
import campus_nexus.enums.TicketStatus;
import campus_nexus.repository.MaintenanceTicketRepository;
import campus_nexus.repository.ResourceRepository;
import campus_nexus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service class for Maintenance Tickets.
 * Handles business logic for reporting issues, updating status,
 * technician assignment, and workflow management.
 * Workflow: OPEN → IN_PROGRESS → RESOLVED → CLOSED
 */
@Service
public class MaintenanceTicketService {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceTicketService.class);

    @Autowired
    private MaintenanceTicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private MongoDocumentPreparer mongoDocumentPreparer;

    /**
     * Create a new maintenance ticket with image attachments support
     * @param request Ticket request DTO
     * @return Saved ticket response DTO
     */
    public TicketResponseDTO createTicket(TicketRequestDTO request) {
        logger.info("Creating new maintenance ticket for resource ID: {}, user ID: {}", request.getResourceId(), request.getUserId());

        // Validation: Ensure reporting user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        // Validation: Ensure resource exists
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + request.getResourceId()));

        // Create and populate ticket
        MaintenanceTicket ticket = new MaintenanceTicket();
        ticket.setUser(user);
        ticket.setResource(resource);
        ticket.setCategory(request.getCategory());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : PriorityLevel.MEDIUM);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setContactPhone(request.getContactPhone());
        ticket.setContactEmail(request.getContactEmail());

        // Handle image URLs (max 3)
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            ticket.setImageUrls(request.getImageUrls());
        }

        MaintenanceTicket savedTicket = ticketRepository.save(mongoDocumentPreparer.prepare(ticket));
        logger.info("Ticket created successfully with ID: {}", savedTicket.getId());

        // Audit log
        auditLogService.log("CREATE_TICKET", user.getEmail(),
                "Ticket ID " + savedTicket.getId() + " created for " + resource.getName());

        return convertToResponseDTO(savedTicket);
    }

    /**
     * Get all tickets with pagination
     */
    public Page<TicketResponseDTO> getAllTickets(Pageable pageable) {
        logger.debug("Fetching all tickets with pagination");
        Page<MaintenanceTicket> ticketPage = ticketRepository.findAll(pageable);
        return ticketPage.map(this::convertToResponseDTO);
    }

    /**
     * Get ticket by ID
     */
    public TicketResponseDTO getTicketById(Long id) {
        logger.debug("Fetching ticket by ID: {}", id);
        MaintenanceTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + id));
        return convertToResponseDTO(ticket);
    }

    /**
     * Get tickets by user ID
     */
    public Page<TicketResponseDTO> getTicketsByUser(Long userId, Pageable pageable) {
        logger.debug("Fetching tickets for user ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        Page<MaintenanceTicket> ticketPage = ticketRepository.findByUserId(userId, pageable);
        return ticketPage.map(this::convertToResponseDTO);
    }

    /**
     * Get tickets by status
     */
    public Page<TicketResponseDTO> getTicketsByStatus(TicketStatus status, Pageable pageable) {
        logger.debug("Fetching tickets with status: {}", status);
        Page<MaintenanceTicket> ticketPage = ticketRepository.findByStatus(status, pageable);
        return ticketPage.map(this::convertToResponseDTO);
    }

    /**
     * Assign a technician to a ticket
     */
    public TicketResponseDTO assignTechnician(Long ticketId, Long technicianId, String adminEmail) {
        logger.info("Assigning technician ID: {} to ticket ID: {}", technicianId, ticketId);

        MaintenanceTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + ticketId));

        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with ID: " + technicianId));

        // Verify user is a technician (optional - can be relaxed)
        // if (technician.getRole() != Role.TECHNICIAN) {
        //     throw new RuntimeException("User is not a technician");
        // }

        ticket.setTechnician(technician);
        MaintenanceTicket updatedTicket = ticketRepository.save(mongoDocumentPreparer.prepare(ticket));

        auditLogService.log("ASSIGN_TECHNICIAN", adminEmail,
                "Technician " + technician.getEmail() + " assigned to ticket ID " + ticketId);

        return convertToResponseDTO(updatedTicket);
    }

    /**
     * Update ticket status with workflow validation
     */
    public TicketResponseDTO updateTicketStatus(Long id, TicketStatus newStatus, String userEmail, String role) {
        logger.info("Updating ticket ID: {} to status: {} by: {} (role: {})", id, newStatus, userEmail, role);

        MaintenanceTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + id));

        // Validate status transition
        validateStatusTransition(ticket.getStatus(), newStatus, role);

        // Update status
        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);

        // Set resolved time if status is RESOLVED
        if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        // Set closed time if status is CLOSED
        if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        }

        MaintenanceTicket updatedTicket = ticketRepository.save(mongoDocumentPreparer.prepare(ticket));

        auditLogService.log("UPDATE_TICKET_STATUS", userEmail,
                "Ticket ID " + id + " status changed from " + oldStatus + " to " + newStatus);

        return convertToResponseDTO(updatedTicket);
    }

    /**
     * Add resolution notes to a ticket (technician action)
     */
    public TicketResponseDTO addResolutionNotes(Long id, String resolutionNotes, String technicianEmail) {
        logger.info("Adding resolution notes to ticket ID: {} by: {}", id, technicianEmail);

        MaintenanceTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + id));

        ticket.setResolutionNotes(resolutionNotes);
        MaintenanceTicket updatedTicket = ticketRepository.save(mongoDocumentPreparer.prepare(ticket));

        auditLogService.log("ADD_RESOLUTION_NOTES", technicianEmail,
                "Resolution notes added to ticket ID " + id);

        return convertToResponseDTO(updatedTicket);
    }

    /**
     * Reject a ticket with reason (admin action)
     */
    public TicketResponseDTO rejectTicket(Long id, String rejectionReason, String adminEmail) {
        logger.info("Rejecting ticket ID: {} by admin: {}", id, adminEmail);

        MaintenanceTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + id));

        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new RuntimeException("Only OPEN tickets can be rejected");
        }

        ticket.setStatus(TicketStatus.REJECTED);
        ticket.setRejectionReason(rejectionReason);
        MaintenanceTicket updatedTicket = ticketRepository.save(mongoDocumentPreparer.prepare(ticket));

        auditLogService.log("REJECT_TICKET", adminEmail,
                "Ticket ID " + id + " rejected. Reason: " + rejectionReason);

        return convertToResponseDTO(updatedTicket);
    }

    /**
     * Delete a ticket by ID
     */
    public void deleteTicket(Long id, String userEmail, String role) {
        logger.info("Deleting ticket ID: {} by: {} (role: {})", id, userEmail, role);

        if (!ticketRepository.existsById(id)) {
            throw new RuntimeException("Ticket not found with ID: " + id);
        }
        ticketRepository.deleteById(id);

        auditLogService.log("DELETE_TICKET", userEmail,
                "Ticket ID " + id + " was deleted");
    }

    /**
     * Search tickets with multiple filters (admin dashboard)
     */
    public Page<TicketResponseDTO> searchTickets(
            Long userId,
            Long resourceId,
            Long technicianId,
            TicketStatus status,
            PriorityLevel priority,
            String category,
            String search,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        logger.debug("Searching tickets with filters");

        Page<MaintenanceTicket> ticketPage = ticketRepository.searchTickets(
                userId, resourceId, technicianId, status, priority, category, search, startDate, endDate, pageable);

        return ticketPage.map(this::convertToResponseDTO);
    }

    /**
     * Validate status transition based on workflow rules
     */
    private void validateStatusTransition(TicketStatus current, TicketStatus next, String role) {
        // Admin can do anything
        if ("ADMIN".equals(role)) {
            return;
        }

        // Technician can only update to IN_PROGRESS or RESOLVED
        if ("TECHNICIAN".equals(role)) {
            if (current == TicketStatus.OPEN && next == TicketStatus.IN_PROGRESS) {
                return;
            }
            if (current == TicketStatus.IN_PROGRESS && next == TicketStatus.RESOLVED) {
                return;
            }
            throw new RuntimeException("Technician cannot change status from " + current + " to " + next);
        }

        // Regular user can only close their own RESOLVED tickets
        if ("USER".equals(role)) {
            if (current == TicketStatus.RESOLVED && next == TicketStatus.CLOSED) {
                return;
            }
            throw new RuntimeException("User can only close resolved tickets");
        }

        throw new RuntimeException("Invalid status transition from " + current + " to " + next);
    }

    /**
     * Convert MaintenanceTicket entity to TicketResponseDTO
     */
    private TicketResponseDTO convertToResponseDTO(MaintenanceTicket ticket) {
        return TicketResponseDTO.builder()
                .id(ticket.getId())
                .userId(ticket.getUser().getId())
                .userEmail(ticket.getUser().getEmail())
                .userName(ticket.getUser().getName())
                .resourceId(ticket.getResource().getId())
                .resourceName(ticket.getResource().getName())
                .resourceLocation(ticket.getResource().getLocation())
                .technicianId(ticket.getTechnician() != null ? ticket.getTechnician().getId() : null)
                .technicianName(ticket.getTechnician() != null ? ticket.getTechnician().getName() : null)
                .category(ticket.getCategory())
                .description(ticket.getDescription())
                .resolutionNotes(ticket.getResolutionNotes())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .contactPhone(ticket.getContactPhone())
                .contactEmail(ticket.getContactEmail())
                .imageUrls(ticket.getImageUrls())
                .rejectionReason(ticket.getRejectionReason())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .closedAt(ticket.getClosedAt())
                .build();
    }
}
