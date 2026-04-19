package campus_nexus.repository;

import campus_nexus.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, Long>, AuditLogRepositoryCustom {

    Page<AuditLog> findByPerformedBy(String performedBy, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    Page<AuditLog> findByActionAndPerformedBy(String action, String performedBy, Pageable pageable);

    Page<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<AuditLog> findByTimestampAfter(LocalDateTime last24Hours);

    long deleteByTimestampBefore(LocalDateTime dateThreshold);
}
