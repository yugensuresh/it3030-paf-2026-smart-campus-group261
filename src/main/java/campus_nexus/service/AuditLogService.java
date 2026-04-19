package campus_nexus.service;

import campus_nexus.entity.AuditLog;
import campus_nexus.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service to handle the creation and retrieval of system audit logs.
 */
@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Records a new action in the database.
     * @param action The type of action performed
     * @param performedBy The identifier of the user (email/username)
     * @param details Specific details about the action
     */
    public void log(String action, String performedBy, String details) {
        AuditLog entry = new AuditLog(action, performedBy, details);
        auditLogRepository.save(entry);
    }

    /**
     * Returns all recorded logs.
     */
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    /**
     * Clears the entire audit trail history.
     */
    public void clearAllLogs() {
        auditLogRepository.deleteAll();
    }
}