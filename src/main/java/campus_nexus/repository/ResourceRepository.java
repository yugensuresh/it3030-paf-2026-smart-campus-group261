package campus_nexus.repository;

import campus_nexus.entity.Resource;
import campus_nexus.enums.ResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    /**
     * Check if a resource exists by name (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find resource by ID (returns Optional)
     */
    Optional<Resource> findById(Long id);

    /**
     * Get active resources with pagination
     */
    Page<Resource> findByStatus(String status, Pageable pageable);

    /**
     * Advanced search with filters and pagination
     * The query uses named parameters for all filters
     */
    @Query("SELECT r FROM Resource r WHERE " +
            "(:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:type IS NULL OR r.type = :type) AND " +
            "(:minCapacity IS NULL OR r.capacity >= :minCapacity) AND " +
            "(:maxCapacity IS NULL OR r.capacity <= :maxCapacity) AND " +
            "(:location IS NULL OR LOWER(r.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:hasWifi IS NULL OR r.hasWifi = :hasWifi) AND " +
            "(:hasAc IS NULL OR r.hasAc = :hasAc)")
    Page<Resource> searchWithFilters(
            @Param("name") String name,
            @Param("type") ResourceType type,
            @Param("minCapacity") Integer minCapacity,
            @Param("maxCapacity") Integer maxCapacity,
            @Param("location") String location,
            @Param("status") String status,
            @Param("hasWifi") Boolean hasWifi,
            @Param("hasAc") Boolean hasAc,
            Pageable pageable);
}