package campus_nexus.repository;

import campus_nexus.entity.Notification;
import campus_nexus.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, Long>, NotificationRepositoryCustom {

    @Query("{ 'user.id': ?0 }")
    Page<Notification> findByUserId(Long userId, Pageable pageable);

    @Query("{ 'user.id': ?0, 'isRead': ?1 }")
    Page<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead, Pageable pageable);

    @Query("{ 'user.id': ?0, 'isRead': ?1 }")
    List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead);

    @Query(value = "{ 'user.id': ?0, 'isRead': ?1 }", count = true)
    long countByUserIdAndIsRead(Long userId, Boolean isRead);

    @Query(value = "{ 'user.id': ?0, 'type': ?1 }", count = true)
    long countByUserIdAndType(Long userId, NotificationType type);

    @Query("{ 'user.id': ?0, 'type': ?1 }")
    Page<Notification> findByUserIdAndType(Long userId, NotificationType type, Pageable pageable);

    List<Notification> findByReferenceId(String referenceId);

    @Query("{ 'user.id': ?0, 'createdAt': { '$gt': ?1 } }")
    List<Notification> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime dateTime);

    long deleteByCreatedAtBefore(LocalDateTime dateThreshold);
}
