package repository;

import entity.enums.OrderStatus;
import entity.enums.OrderType;
import entity.order.Order;
import entity.lesson.Lesson;
import entity.order.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
            "FROM Order o " +
            "JOIN o.items oi " +
            "WHERE o.user.id = :userId " +
            "AND oi.lesson.id = :lessonId " +
            "AND o.status = 'COMPLETED' " +
            "AND o.orderType = 'SINGLE_PURCHASE'")
    boolean existsCompletedOrderForLesson(
            @Param("userId") Long userId,
            @Param("lessonId") Long lessonId
    );

    @Query("SELECT oi.lesson " +
            "FROM Order o " +
            "JOIN o.items oi " +
            "WHERE o.user.id = :userId " +
            "AND oi.lesson IS NOT NULL " +
            "AND o.status = 'COMPLETED' " +
            "AND o.orderType = 'SINGLE_PURCHASE' " +
            "GROUP BY oi.lesson " + // Групуємо по уроку
            "ORDER BY MAX(o.createdAt) DESC") // Сортуємо за останньою покупкою
    List<Lesson> findPurchasedLessonsByUserId(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o " +
            "WHERE o.user.id = :userId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByUserId(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o " +
            "WHERE o.user.id = :userId " +
            "AND o.status = :status " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") entity.enums.OrderStatus status
    );

    /**
     * Завантажити замовлення разом із підписками
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.subscriptions " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithSubscription(@Param("id") Long id);

    /**
     * Крок 1: Завантажуємо Order та його Items
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.items " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    /**
     * Крок 2: Окремо підтягуємо деталі планів та переклади для OrderItem
     */
    @Query("SELECT DISTINCT i FROM OrderItem i " +
            "LEFT JOIN FETCH i.subscriptionPlan sp " +
            "LEFT JOIN FETCH sp.translations " +
            "WHERE i.order.id = :orderId")
    List<OrderItem> fetchOrderItemDetails(@Param("orderId") Long orderId);

    // ========== НОВІ МЕТОДИ ДЛЯ АДМІНКИ ==========

    /**
     * ВИПРАВЛЕНО: Додано явний countQuery
     */
    @Query(value = "SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.user u " +
            "LEFT JOIN FETCH u.emails",
            countQuery = "SELECT COUNT(DISTINCT o) FROM Order o")
    Page<Order> findAllWithDetails(Pageable pageable);

    /**
     * Завантажити Order з User та PromoCode (БЕЗ колекцій!)
     */
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.user u " +
            "LEFT JOIN FETCH u.emails " +
            "LEFT JOIN FETCH o.promoCode " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithUser(@Param("id") Long id);

    /**
     * Окремо завантажити Items для Order (з деталями)
     */
    @Query("SELECT DISTINCT i FROM OrderItem i " +
            "LEFT JOIN FETCH i.lesson l " +
            "LEFT JOIN FETCH l.translations " +
            "LEFT JOIN FETCH i.subscriptionPlan sp " +
            "LEFT JOIN FETCH sp.translations " +
            "WHERE i.order.id = :orderId")
    List<OrderItem> findItemsWithDetailsByOrderId(@Param("orderId") Long orderId);

    /**
     * ВИПРАВЛЕНО: Додано явний countQuery для фільтрації за статусом
     */
    @Query(value = "SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.user u " +
            "LEFT JOIN FETCH u.emails " +
            "WHERE o.status = :status",
            countQuery = "SELECT COUNT(DISTINCT o) FROM Order o WHERE o.status = :status")
    Page<Order> findByStatus(@Param("status") OrderStatus status, Pageable pageable);

    /**
     * ВИПРАВЛЕНО: Додано явний countQuery для фільтрації за типом
     */
    @Query(value = "SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.user u " +
            "LEFT JOIN FETCH u.emails " +
            "WHERE o.orderType = :type",
            countQuery = "SELECT COUNT(DISTINCT o) FROM Order o WHERE o.orderType = :type")
    Page<Order> findByOrderType(@Param("type") OrderType type, Pageable pageable);

    /**
     * ВИПРАВЛЕНО: Додано явний countQuery для фільтрації за датою
     */
    @Query(value = "SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.user u " +
            "LEFT JOIN FETCH u.emails " +
            "WHERE o.createdAt BETWEEN :from AND :to",
            countQuery = "SELECT COUNT(DISTINCT o) FROM Order o " +
                    "WHERE o.createdAt BETWEEN :from AND :to")
    Page<Order> findByCreatedAtBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    /**
     * ВИПРАВЛЕНО: Додано явний countQuery для комплексного фільтра
     */
    @Query(value = "SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.user u " +
            "LEFT JOIN FETCH u.emails " +
            "WHERE (:status IS NULL OR o.status = :status) " +
            "AND (:type IS NULL OR o.orderType = :type) " +
            "AND (:from IS NULL OR o.createdAt >= :from) " +
            "AND (:to IS NULL OR o.createdAt <= :to)",
            countQuery = "SELECT COUNT(DISTINCT o) FROM Order o " +
                    "WHERE (:status IS NULL OR o.status = :status) " +
                    "AND (:type IS NULL OR o.orderType = :type) " +
                    "AND (:from IS NULL OR o.createdAt >= :from) " +
                    "AND (:to IS NULL OR o.createdAt <= :to)")
    Page<Order> findWithFilters(
            @Param("status") OrderStatus status,
            @Param("type") OrderType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    /**
     * ВИПРАВЛЕНО: Додано явний countQuery для пошуку за email
     */
    @Query(value = "SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.user u " +
            "LEFT JOIN FETCH u.emails e " +
            "WHERE LOWER(e.email) LIKE LOWER(CONCAT('%', :email, '%'))",
            countQuery = "SELECT COUNT(DISTINCT o) FROM Order o " +
                    "JOIN o.user u " +
                    "JOIN u.emails e " +
                    "WHERE LOWER(e.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    Page<Order> findByUserEmail(@Param("email") String email, Pageable pageable);

    // ========== СТАТИСТИКА ==========

    /**
     * Загальна сума всіх завершених замовлень
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.status = 'COMPLETED'")
    Long getTotalRevenue();

    /**
     * Кількість замовлень за статусом
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);

    /**
     * Кількість замовлень за типом
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderType = :type")
    Long countByOrderType(@Param("type") OrderType type);

    /**
     * Середній чек
     */
    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED'")
    Double getAverageOrderAmount();

    /**
     * Замовлення за останні N днів
     */
    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE o.createdAt >= :since")
    Long countOrdersSince(@Param("since") LocalDateTime since);

    /**
     * Дохід за період
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.status = 'COMPLETED' " +
            "AND o.createdAt BETWEEN :from AND :to")
    Long getRevenueBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}