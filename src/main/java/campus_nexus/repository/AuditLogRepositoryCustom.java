package campus_nexus.repository;

import campus_nexus.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepositoryCustom {
    Page<AuditLog> searchAuditLogs(String action, String performedBy, LocalDateTime startDate,
                                   LocalDateTime endDate, Pageable pageable);

    List<Object[]> countLogsByAction();

    List<Object[]> countLogsByUser();
}
