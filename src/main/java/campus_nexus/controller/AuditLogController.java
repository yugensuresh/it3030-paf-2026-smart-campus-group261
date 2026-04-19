package campus_nexus.controller;

import campus_nexus.entity.AuditLog;
import campus_nexus.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AuditLogController provides endpoints for Administrators to monitor system activity.
 * Essential for meeting the "Security & Auditability" technical constraint.
 */
@RestController
@RequestMapping("/api/admin/logs")
@CrossOrigin(origins = "*")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Fetch all system logs for the Audit Table view.
     * URL: GET http://localhost:8080/api/admin/logs
     */
    @GetMapping
    public List<AuditLog> getLogs() {
        return auditLogService.getAllLogs();
    }

    /**
     * Clear all logs (Optional Admin feature for maintenance).
     * URL: DELETE http://localhost:8080/api/admin/logs
     */
    @DeleteMapping("/clear")
    public String clearLogs() {
        auditLogService.clearAllLogs();
        return "Audit logs cleared successfully!";
    }
}