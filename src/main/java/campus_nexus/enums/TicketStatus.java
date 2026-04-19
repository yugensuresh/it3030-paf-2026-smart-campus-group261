package campus_nexus.enums;

/**
 * TicketStatus enum defines the lifecycle of a maintenance ticket.
 * Workflow: OPEN → IN_PROGRESS → RESOLVED → CLOSED
 * Admin may also set REJECTED with a reason.
 */
public enum TicketStatus {

    OPEN("Open - Waiting for assignment"),
    IN_PROGRESS("In Progress - Technician working on it"),
    RESOLVED("Resolved - Fix completed, pending closure"),
    CLOSED("Closed - Ticket completed and closed"),
    REJECTED("Rejected - Request denied by admin");

    private final String description;

    TicketStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if status is a terminal state (cannot be changed further)
     */
    public boolean isTerminal() {
        return this == CLOSED || this == REJECTED;
    }

    /**
     * Get next allowed statuses for workflow
     */
    public TicketStatus[] getAllowedNextStatuses() {
        switch (this) {
            case OPEN:
                return new TicketStatus[]{IN_PROGRESS, REJECTED};
            case IN_PROGRESS:
                return new TicketStatus[]{RESOLVED};
            case RESOLVED:
                return new TicketStatus[]{CLOSED};
            case CLOSED:
            case REJECTED:
                return new TicketStatus[]{};
            default:
                return new TicketStatus[]{};
        }
    }
}