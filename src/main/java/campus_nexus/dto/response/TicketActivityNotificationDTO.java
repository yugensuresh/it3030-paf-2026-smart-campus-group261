package campus_nexus.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Payload for real-time ticket activity (technician status / notes) over SSE.
 */
@Data
@Builder
public class TicketActivityNotificationDTO {

    private Long ticketId;
    /** Reporting user who should receive the notification. */
    private Long ticketOwnerUserId;
    private LocalDateTime ticketCreatedAt;
    /** When the technician performed the action. */
    private LocalDateTime eventAt;
    private String note;
    /** Human-readable action, e.g. "Status → IN_PROGRESS" or "Resolution notes updated". */
    private String action;
}
