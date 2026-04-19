package campus_nexus.enums;

/**
 * NotificationType enum defines all possible notification categories
 * in the Campus Nexus operations hub.
 */
public enum NotificationType {

    // Booking related notifications
    BOOKING_PENDING("Booking Request Received", "Your booking request has been submitted and is pending approval"),
    BOOKING_APPROVED("Booking Approved", "Your booking has been approved by admin"),
    BOOKING_REJECTED("Booking Rejected", "Your booking request has been rejected"),
    BOOKING_CANCELLED("Booking Cancelled", "Your booking has been cancelled"),
    BOOKING_REMINDER("Booking Reminder", "You have a booking scheduled soon"),

    // Ticket related notifications
    TICKET_CREATED("Ticket Created", "Your maintenance ticket has been created"),
    TICKET_ASSIGNED("Ticket Assigned", "A technician has been assigned to your ticket"),
    TICKET_STATUS_UPDATED("Ticket Status Updated", "Your ticket status has been updated"),
    TICKET_RESOLVED("Ticket Resolved", "Your ticket has been resolved"),
    TICKET_REJECTED("Ticket Rejected", "Your ticket has been rejected"),
    TICKET_CLOSED("Ticket Closed", "Your ticket has been closed"),

    // Comment related notifications
    COMMENT_ADDED("New Comment", "Someone added a comment to your ticket"),
    COMMENT_EDITED("Comment Edited", "A comment on your ticket was edited"),
    COMMENT_DELETED("Comment Deleted", "A comment on your ticket was deleted"),

    // General notifications
    SYSTEM_ALERT("System Alert", "Important system notification"),
    REMINDER("Reminder", "Action required");

    private final String title;
    private final String defaultMessage;

    NotificationType(String title, String defaultMessage) {
        this.title = title;
        this.defaultMessage = defaultMessage;
    }

    public String getTitle() {
        return title;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    /**
     * Check if notification is booking related
     */
    public boolean isBookingRelated() {
        return this.name().startsWith("BOOKING");
    }

    /**
     * Check if notification is ticket related
     */
    public boolean isTicketRelated() {
        return this.name().startsWith("TICKET");
    }

    /**
     * Check if notification is comment related
     */
    public boolean isCommentRelated() {
        return this.name().startsWith("COMMENT");
    }
}