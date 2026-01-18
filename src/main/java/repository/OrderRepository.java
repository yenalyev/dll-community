package repository;

import entity.order.Order;
import entity.lesson.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Перевірити чи користувач придбав конкретний урок
     *
     * @param userId ID користувача
     * @param lessonId ID уроку
     * @return true якщо є завершене замовлення з цим уроком
     */
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

    /**
     * Отримати всі придбані уроки користувача
     * Корисно для сторінки "Мої уроки"
     *
     * @param userId ID користувача
     * @return список придбаних уроків
     */
    @Query("SELECT DISTINCT oi.lesson " +
            "FROM Order o " +
            "JOIN o.items oi " +
            "WHERE o.user.id = :userId " +
            "AND oi.lesson IS NOT NULL " +
            "AND o.status = 'COMPLETED' " +
            "AND o.orderType = 'SINGLE_PURCHASE' " +
            "ORDER BY o.createdAt DESC")
    List<Lesson> findPurchasedLessonsByUserId(@Param("userId") Long userId);

    /**
     * Знайти всі замовлення користувача
     *
     * @param userId ID користувача
     * @return список замовлень
     */
    @Query("SELECT o FROM Order o " +
            "WHERE o.user.id = :userId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByUserId(@Param("userId") Long userId);

    /**
     * Знайти замовлення користувача по статусу
     *
     * @param userId ID користувача
     * @param status статус замовлення
     * @return список замовлень
     */
    @Query("SELECT o FROM Order o " +
            "WHERE o.user.id = :userId " +
            "AND o.status = :status " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") entity.enums.OrderStatus status
    );
}