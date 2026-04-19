package campus_nexus.repository;

public interface NotificationRepositoryCustom {
    int markAllAsRead(Long userId);

    int markAsRead(Long id, Long userId);

    void deleteByUserId(Long userId);
}
