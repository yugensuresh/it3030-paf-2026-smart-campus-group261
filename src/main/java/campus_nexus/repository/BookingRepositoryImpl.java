package campus_nexus.repository;

import campus_nexus.entity.Booking;
import campus_nexus.enums.BookingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BookingRepositoryImpl implements BookingRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public BookingRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Booking> findConflictingBookings(Long resourceId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Query query = new Query();
        query.addCriteria(new Criteria().andOperator(
                Criteria.where("resource.id").is(resourceId),
                Criteria.where("bookingDate").is(date),
                Criteria.where("status").nin(BookingStatus.CANCELLED, BookingStatus.REJECTED),
                Criteria.where("startTime").lt(endTime),
                Criteria.where("endTime").gt(startTime)
        ));
        return mongoTemplate.find(query, Booking.class);
    }

    @Override
    public Page<Booking> searchBookings(Long userId, Long resourceId, BookingStatus status,
                                        LocalDate startDate, LocalDate endDate, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (userId != null) {
            criteriaList.add(Criteria.where("user.id").is(userId));
        }
        if (resourceId != null) {
            criteriaList.add(Criteria.where("resource.id").is(resourceId));
        }
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }
        if (startDate != null) {
            criteriaList.add(Criteria.where("bookingDate").gte(startDate));
        }
        if (endDate != null) {
            criteriaList.add(Criteria.where("bookingDate").lte(endDate));
        }

        Query query = new Query().with(pageable);
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        List<Booking> bookings = mongoTemplate.find(query, Booking.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Booking.class);
        return new PageImpl<>(bookings, pageable, total);
    }
}
