package repository;

import entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Знайти всі items замовлення
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Знайти items з певним уроком
     */
    List<OrderItem> findByLessonId(Long lessonId);

    /**
     * Знайти items з певним планом підписки
     */
    List<OrderItem> findBySubscriptionPlanId(Long planId);
}