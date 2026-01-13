package repository;

import entity.enums.OrderStatus;
import entity.enums.OrderType;
import entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Знайти замовлення користувача
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Знайти замовлення по статусу
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Знайти замовлення по типу
     */
    List<Order> findByOrderType(OrderType orderType);

    /**
     * Знайти замовлення користувача по статусу
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Знайти замовлення що очікують оплати довше ніж N хвилин
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' " +
            "AND o.createdAt < :deadline")
    List<Order> findPendingOrdersOlderThan(@Param("deadline") LocalDateTime deadline);

    /**
     * Статистика замовлень за період
     */
    @Query("SELECT COUNT(o), SUM(o.totalAmount) FROM Order o " +
            "WHERE o.status = 'COMPLETED' " +
            "AND o.createdAt BETWEEN :startDate AND :endDate")
    Object[] getOrderStatistics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Знайти останнє замовлення користувача
     */
    Order findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Підрахувати кількість завершених замовлень користувача
     */
    long countByUserIdAndStatus(Long userId, OrderStatus status);
}