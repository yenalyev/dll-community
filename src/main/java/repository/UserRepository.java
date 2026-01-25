package repository;

import entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"emails", "role", "settings"})
    Optional<User> findWithDetailsById(Long id);

    @Query("SELECT u FROM User u " +
            "JOIN FETCH u.emails e " +
            "JOIN FETCH u.role " +
            "WHERE e.email = :email AND e.isPrimary = true")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u " +
            "JOIN FETCH u.socialAccounts sa " +
            "WHERE sa.provider = :provider AND sa.providerUserId = :providerUserId")
    Optional<User> findBySocialAccount(
            @Param("provider") String provider,
            @Param("providerUserId") String providerUserId
    );

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM UserEmail e WHERE e.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.emails " +
            "LEFT JOIN FETCH u.settings " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH u.socialAccounts " +
            "WHERE u.id = :userId")
    User findByIdWithAllData(@Param("userId") Long userId);



    // ========== НОВІ МЕТОДИ ДЛЯ АДМІНКИ ==========

    /**
     * ВИПРАВЛЕНО: Додано явний countQuery
     */
    @Query(value = "SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.role " +
            "LEFT JOIN FETCH u.emails",
            countQuery = "SELECT COUNT(DISTINCT u) FROM User u")
    Page<User> findAllWithDetails(Pageable pageable);

    /**
     * Пошук користувачів за email (БЕЗ JOIN FETCH - OK)
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.emails e " +
            "WHERE LOWER(e.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    Page<User> findByEmailContaining(@Param("email") String email, Pageable pageable);

    /**
     * Пошук користувачів за ім'ям (БЕЗ JOIN FETCH - OK)
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<User> findByNameContaining(@Param("name") String name, Pageable pageable);

    /**
     * ВИПРАВЛЕНО: Додано явний countQuery для фільтрації за роллю
     */
    @Query(value = "SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.role r " +
            "WHERE r.name = :roleName",
            countQuery = "SELECT COUNT(DISTINCT u) FROM User u " +
                    "JOIN u.role r " +
                    "WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

    /**
     * ВИПРАВЛЕНО: Додано явний countQuery для фільтрації за статусом
     */
    @Query(value = "SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.role " +
            "WHERE u.isActive = :isActive",
            countQuery = "SELECT COUNT(DISTINCT u) FROM User u " +
                    "WHERE u.isActive = :isActive")
    Page<User> findByIsActive(@Param("isActive") Boolean isActive, Pageable pageable);

    /**
     * Користувачі з активними підписками (БЕЗ JOIN FETCH - OK)
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.subscriptions s " +
            "WHERE s.status = 'ACTIVE' " +
            "AND s.endDate > :now " +
            "ORDER BY u.createdAt DESC")
    Page<User> findUsersWithActiveSubscriptions(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    /**
     * Користувачі без активних підписок (БЕЗ JOIN FETCH - OK)
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "WHERE u.id NOT IN (" +
            "  SELECT DISTINCT s.user.id FROM UserSubscription s " +
            "  WHERE s.status = 'ACTIVE' AND s.endDate > :now" +
            ") " +
            "ORDER BY u.createdAt DESC")
    Page<User> findUsersWithoutActiveSubscriptions(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    /**
     * Користувачі зареєстровані за період (БЕЗ JOIN FETCH - OK)
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "WHERE u.createdAt BETWEEN :from AND :to " +
            "ORDER BY u.createdAt DESC")
    Page<User> findByCreatedAtBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    // ========== СТАТИСТИКА ==========

    /**
     * Загальна кількість активних користувачів
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();

    /**
     * Кількість користувачів з активними підписками
     */
    @Query("SELECT COUNT(DISTINCT s.user.id) FROM UserSubscription s " +
            "WHERE s.status = 'ACTIVE' AND s.endDate > :now")
    Long countUsersWithActiveSubscriptions(@Param("now") LocalDateTime now);

    /**
     * Нові користувачі за період
     */
    @Query("SELECT COUNT(u) FROM User u " +
            "WHERE u.createdAt >= :since")
    Long countUsersSince(@Param("since") LocalDateTime since);

    /**
     * Кількість користувачів за роллю
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = :roleName")
    Long countByRoleName(@Param("roleName") String roleName);

    /**
     * Отримати список користувачів з їхньою статистикою замовлень
     */
    @Query("SELECT u, " +
            "COUNT(DISTINCT o.id) as orderCount, " +
            "COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN o.totalAmount ELSE 0 END), 0) as totalSpent " +
            "FROM User u " +
            "LEFT JOIN Order o ON o.user.id = u.id " +
            "GROUP BY u.id")
    List<Object[]> findUsersWithOrderStatistics();


    /**
     * Останні зареєстровані користувачі
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.emails " +
            "ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(Pageable pageable);
}