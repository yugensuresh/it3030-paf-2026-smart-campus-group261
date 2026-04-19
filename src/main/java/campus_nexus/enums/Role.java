package campus_nexus.enums;

/**
 * Role enum defines user roles for Role-Based Access Control (RBAC).
 * Supports: USER, ADMIN, TECHNICIAN, MANAGER
 *
 * Usage:
 * - USER: Regular user who can book resources and create tickets
 * - ADMIN: Full system access - can approve bookings, assign technicians
 * - TECHNICIAN: Can update ticket status and add resolution notes
 * - MANAGER: Can view reports and manage resources (optional extension)
 */
public enum Role {

    USER("User", "Regular user who can book resources and create tickets"),
    ADMIN("Administrator", "Full system access - can approve bookings, assign technicians, manage all resources"),
    TECHNICIAN("Technician", "Can update ticket status, add resolution notes, and resolve issues"),
    MANAGER("Manager", "Can view reports, analytics, and manage resources");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if role has admin-level privileges
     * @return true if role is ADMIN or MANAGER
     */
    public boolean isAdmin() {
        return this == ADMIN || this == MANAGER;
    }

    /**
     * Check if role can approve booking requests
     * @return true if role is ADMIN
     */
    public boolean canApproveBookings() {
        return this == ADMIN;
    }

    /**
     * Check if role can manage resources (create, update, delete)
     * @return true if role is ADMIN or MANAGER
     */
    public boolean canManageResources() {
        return this == ADMIN || this == MANAGER;
    }

    /**
     * Check if role can handle maintenance tickets
     * @return true if role is ADMIN or TECHNICIAN
     */
    public boolean canHandleTickets() {
        return this == ADMIN || this == TECHNICIAN;
    }

    /**
     * Check if role can assign technicians to tickets
     * @return true if role is ADMIN
     */
    public boolean canAssignTechnicians() {
        return this == ADMIN;
    }

    /**
     * Check if role can view audit logs
     * @return true if role is ADMIN or MANAGER
     */
    public boolean canViewAuditLogs() {
        return this == ADMIN || this == MANAGER;
    }

    /**
     * Get role priority level (higher number = more privileges)
     * Used for role hierarchy validation
     * @return priority level (1=USER, 2=TECHNICIAN, 3=MANAGER, 4=ADMIN)
     */
    public int getPriority() {
        switch (this) {
            case USER:
                return 1;
            case TECHNICIAN:
                return 2;
            case MANAGER:
                return 3;
            case ADMIN:
                return 4;
            default:
                return 0;
        }
    }

    /**
     * Check if this role has higher or equal priority than given role
     * @param otherRole Role to compare against
     * @return true if this role has priority >= other role
     */
    public boolean hasHigherOrEqualPriorityThan(Role otherRole) {
        return this.getPriority() >= otherRole.getPriority();
    }

    /**
     * Get Role from display name
     * @param displayName Display name of the role
     * @return Matching Role or USER as default
     */
    public static Role fromDisplayName(String displayName) {
        for (Role role : values()) {
            if (role.displayName.equalsIgnoreCase(displayName)) {
                return role;
            }
        }
        return USER;  // Default fallback
    }
}