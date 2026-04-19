package campus_nexus.repository;

import campus_nexus.entity.User;
import campus_nexus.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom query methods for user management.
 * Supports OAuth login, role-based filtering, and admin user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address (used for OAuth login)
     * This is the primary method for Google Sign-In authentication
     *
     * @param email User's email address
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email (used during OAuth registration)
     *
     * @param email User's email address
     * @return true if user exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all users with a specific role (e.g., all ADMIN users)
     * Used for role-based management
     *
     * @param role User role (ADMIN, USER, TECHNICIAN, MANAGER)
     * @return List of users with the specified role
     */
    List<User> findByRole(Role role);

    /**
     * Find users by role with pagination (for admin user management)
     *
     * @param role User role
     * @param pageable Pagination info
     * @return Page of users with the specified role
     */
    Page<User> findByRole(Role role, Pageable pageable);

    /**
     * Search users by name or email (for admin dashboard)
     * Used for "Find User" functionality
     *
     * @param searchTerm Search term to match against name or email
     * @param pageable Pagination info
     * @return Page of users matching the search term
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count users by role (for dashboard statistics)
     *
     * @return List of object arrays containing role and count
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();

    /**
     * Get all ADMIN users (for notification broadcasting, etc.)
     *
     * @return List of admin users
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    List<User> findAllAdmins();

    /**
     * Get all TECHNICIAN users (for ticket assignment)
     *
     * @return List of technician users
     */
    @Query("SELECT u FROM User u WHERE u.role = 'TECHNICIAN'")
    List<User> findAllTechnicians();
}