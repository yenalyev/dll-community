package repository;

import entity.order.Order;
import entity.lesson.Lesson;
import entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT DISTINCT oi.lesson " +
            "FROM Order o " +
            "JOIN o.items oi " +
            "WHERE o.user.id = :userId " +
            "AND oi.lesson IS NOT NULL " +
            "AND o.status = 'COMPLETED' " +
            "AND o.orderType = 'SINGLE_PURCHASE' " +
            "ORDER BY o.createdAt DESC")
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
}