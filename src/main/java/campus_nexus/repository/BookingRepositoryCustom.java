package campus_nexus.repository;

import campus_nexus.entity.Booking;
import campus_nexus.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingRepositoryCustom {
    List<Booking> findConflictingBookings(Long resourceId, LocalDate date, LocalTime startTime, LocalTime endTime);

    Page<Booking> searchBookings(Long userId, Long resourceId, BookingStatus status,
                                 LocalDate startDate, LocalDate endDate, Pageable pageable);
}
