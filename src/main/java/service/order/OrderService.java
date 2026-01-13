package service.order;


import entity.enums.*;
import entity.order.*;
import entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.OrderRepository;
import service.subscription.SubscriptionService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final SubscriptionService subscriptionService;

    /**
     * Створити замовлення на підписку
     */
    @Transactional
    public Order createSubscriptionOrder(User user, SubscriptionPlan plan, Currency currency) {
        log.info("Creating subscription order for user {} with plan {}", user.getId(), plan.getId());

        Long amount = subscriptionService.getPlanPrice(plan, currency);
        if (amount == null) {
            throw new RuntimeException("Price not found for currency: " + currency);
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderType(OrderType.SUBSCRIPTION_PURCHASE);
        order.setTotalAmount(amount);
        order.setCurrency(currency);

        // Створити OrderItem
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setSubscriptionPlan(plan);
        item.setAmount(amount);
        item.setCurrency(currency);

        order.getItems().add(item);

        return orderRepository.save(order);
    }

    /**
     * Завершити замовлення після успішної оплати
     */
    @Transactional
    public void completeOrder(Long orderId, String paymentGateway, String gatewayOrderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.COMPLETED);
        order.setPaymentGateway(paymentGateway);
        order.setCompletedAt(LocalDateTime.now());

        // Якщо це замовлення на підписку - створити підписку
        if (order.getOrderType() == OrderType.SUBSCRIPTION_PURCHASE) {
            OrderItem item = order.getItems().stream()
                    .filter(i -> i.getSubscriptionPlan() != null)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Subscription plan not found in order"));

            subscriptionService.createSubscription(
                    order.getUser(),
                    item.getSubscriptionPlan(),
                    order
            );
        }

        orderRepository.save(order);
        log.info("Order {} completed successfully", orderId);
    }

    /**
     * Скасувати замовлення
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    /**
     * Отримати історію замовлень користувача
     */
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Отримати замовлення по ID
     */
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
