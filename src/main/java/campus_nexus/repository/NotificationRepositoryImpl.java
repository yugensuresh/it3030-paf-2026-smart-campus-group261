package campus_nexus.repository;

import campus_nexus.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public NotificationRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public int markAllAsRead(Long userId) {
        Query query = new Query().addCriteria(new Criteria().andOperator(
                Criteria.where("user.id").is(userId),
                Criteria.where("isRead").is(false)
        ));
        Update update = new Update().set("isRead", true);
        return (int) mongoTemplate.updateMulti(query, update, Notification.class).getModifiedCount();
    }

    @Override
    public int markAsRead(Long id, Long userId) {
        Query query = new Query().addCriteria(new Criteria().andOperator(
                Criteria.where("_id").is(id),
                Criteria.where("user.id").is(userId)
        ));
        Update update = new Update().set("isRead", true);
        return (int) mongoTemplate.updateFirst(query, update, Notification.class).getModifiedCount();
    }

    @Override
    public void deleteByUserId(Long userId) {
        Query query = new Query(Criteria.where("user.id").is(userId));
        mongoTemplate.remove(query, Notification.class);
    }
}
