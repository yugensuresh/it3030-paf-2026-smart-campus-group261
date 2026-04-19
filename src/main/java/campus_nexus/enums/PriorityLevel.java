package campus_nexus.enums;

/**
 * PriorityLevel enum defines the severity of the maintenance issue.
 * Used to categorize tickets for faster response times and SLA tracking.
 */
public enum PriorityLevel {

    LOW("Low", "Minor issue, no immediate urgency", 48),      // 48 hours SLA
    MEDIUM("Medium", "Moderate issue affecting operations", 24),  // 24 hours SLA
    HIGH("High", "Critical issue requiring immediate attention", 4);  // 4 hours SLA

    private final String displayName;
    private final String description;
    private final int responseHours;  // SLA: hours within which to respond

    PriorityLevel(String displayName, String description, int responseHours) {
        this.displayName = displayName;
        this.description = description;
        this.responseHours = responseHours;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getResponseHours() {
        return responseHours;
    }

    /**
     * Get priority level from display name
     */
    public static PriorityLevel fromDisplayName(String displayName) {
        for (PriorityLevel level : values()) {
            if (level.displayName.equalsIgnoreCase(displayName)) {
                return level;
            }
        }
        return MEDIUM;  // Default fallback
    }
}