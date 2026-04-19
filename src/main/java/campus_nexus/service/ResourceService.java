package campus_nexus.service;

import campus_nexus.entity.Resource;
import campus_nexus.enums.ResourceReservationCategory;
import campus_nexus.enums.ResourceType;
import campus_nexus.repository.BookingRepository;
import campus_nexus.repository.MaintenanceTicketRepository;
import campus_nexus.repository.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ResourceService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MaintenanceTicketRepository maintenanceTicketRepository;

    /**
     * Create a new resource
     * @param resource Resource entity to save
     * @return Saved resource with generated ID
     * @throws RuntimeException if resource name already exists
     */
    @Transactional
    public Resource createResource(Resource resource) {
        // Check for duplicate name
        if (resourceRepository.existsByNameIgnoreCase(resource.getName())) {
            logger.warn("Attempted to create duplicate resource: {}", resource.getName());
            throw new RuntimeException("Resource with name '" + resource.getName() + "' already exists");
        }

        // Ensure status is set
        if (resource.getStatus() == null) {
            resource.setStatus("ACTIVE");
        }

        Resource saved = resourceRepository.save(resource);
        logger.info("Created new resource: {} (ID: {})", saved.getName(), saved.getId());
        return saved;
    }

    /**
     * Get all resources (no pagination - for dropdowns/small lists)
     * @return List of all resources
     */
    public List<Resource> getAllResources() {
        logger.debug("Fetching all resources");
        return resourceRepository.findAll();
    }

    /**
     * Get resource by ID
     * @param id Resource ID
     * @return Resource entity
     * @throws RuntimeException if not found
     */
    public Resource getResourceById(Long id) {
        logger.debug("Fetching resource by ID: {}", id);
        return resourceRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Resource not found with ID: {}", id);
                    return new RuntimeException("Resource not found with ID: " + id);
                });
    }

    /**
     * Update existing resource
     * @param id Resource ID to update
     * @param updatedResource Updated resource data
     * @return Updated resource
     * @throws RuntimeException if resource not found or duplicate name
     */
    @Transactional
    public Resource updateResource(Long id, Resource updatedResource) {
        Resource existing = getResourceById(id);

        // Check duplicate name (if name is changing)
        if (!existing.getName().equalsIgnoreCase(updatedResource.getName()) &&
                resourceRepository.existsByNameIgnoreCase(updatedResource.getName())) {
            logger.warn("Cannot update resource {}: name {} already exists", id, updatedResource.getName());
            throw new RuntimeException("Resource with name '" + updatedResource.getName() + "' already exists");
        }

        // Update fields
        existing.setName(updatedResource.getName());
        existing.setType(updatedResource.getType());
        if (updatedResource.getCategory() != null) {
            existing.setCategory(updatedResource.getCategory());
        }
        existing.setCapacity(updatedResource.getCapacity());
        existing.setLocation(updatedResource.getLocation());
        existing.setDescription(updatedResource.getDescription());
        existing.setHasWifi(updatedResource.getHasWifi());
        existing.setHasAc(updatedResource.getHasAc());
        existing.setHasProjector(updatedResource.getHasProjector());
        existing.setStatus(updatedResource.getStatus());

        Resource saved = resourceRepository.save(existing);
        logger.info("Updated resource ID: {} - {}", id, saved.getName());
        return saved;
    }

    /**
     * Delete resource by ID
     * @param id Resource ID to delete
     * @throws RuntimeException if resource not found
     */
    @Transactional(readOnly = false)
    public void deleteResource(Long id) {
        if (!resourceRepository.existsById(id)) {
            logger.warn("Cannot delete - resource not found with ID: {}", id);
            throw new RuntimeException("Resource not found with ID: " + id);
        }
        maintenanceTicketRepository.deleteByResourceId(id);
        bookingRepository.deleteByResourceId(id);
        resourceRepository.deleteById(id);
        logger.info("Deleted resource ID: {} (removed related tickets and bookings first)", id);
    }

    /**
     * Update resource status (ACTIVE, OUT_OF_SERVICE, MAINTENANCE)
     * @param id Resource ID
     * @param status New status
     * @return Updated resource
     */
    @Transactional
    public Resource updateResourceStatus(Long id, String status) {
        Resource resource = getResourceById(id);
        resource.setStatus(status);
        Resource saved = resourceRepository.save(resource);
        logger.info("Updated resource {} status to: {}", id, status);
        return saved;
    }

    /**
     * Advanced search with filters and pagination
     * @param name Search by name (partial match)
     * @param type Filter by resource type
     * @param category Filter by reservation category
     * @param minCapacity Minimum capacity filter
     * @param maxCapacity Maximum capacity filter
     * @param location Filter by location (partial match)
     * @param status Filter by status (ACTIVE/OUT_OF_SERVICE)
     * @param hasWifi Filter by WiFi availability
     * @param hasAc Filter by AC availability
     * @param pageable Pagination and sorting info
     * @return Page of resources matching criteria
     */
    public Page<Resource> searchResources(
            String name,
            ResourceType type,
            ResourceReservationCategory category,
            Integer minCapacity,
            Integer maxCapacity,
            String location,
            String status,
            Boolean hasWifi,
            Boolean hasAc,
            Pageable pageable) {

        logger.debug("Searching resources with filters - name: {}, type: {}, category: {}, minCap: {}, maxCap: {}, location: {}, status: {}, wifi: {}, ac: {}",
                name, type, category, minCapacity, maxCapacity, location, status, hasWifi, hasAc);

        return resourceRepository.searchWithFilters(
                name, type, category, minCapacity, maxCapacity, location, status, hasWifi, hasAc, pageable);
    }

    /**
     * Get active resources only (status = ACTIVE) with pagination
     */
    public Page<Resource> getActiveResources(Pageable pageable) {
        logger.debug("Fetching active resources");
        return resourceRepository.findByStatus("ACTIVE", pageable);
    }
}