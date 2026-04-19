package campus_nexus.repository;

import campus_nexus.entity.Booking;
import campus_nexus.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, Long>, BookingRepositoryCustom {

    @org.springframework.data.mongodb.repository.Query("{ 'user.id': ?0 }")
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    @org.springframework.data.mongodb.repository.Query("{ 'resource.id': ?0 }")
    Page<Booking> findByResourceId(Long resourceId, Pageable pageable);

    @org.springframework.data.mongodb.repository.Query("{ 'resource.id': ?0, 'bookingDate': ?1 }")
    List<Booking> findByResourceIdAndBookingDate(Long resourceId, LocalDate bookingDate);
}
