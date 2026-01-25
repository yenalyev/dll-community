package admin.service;

import admin.dto.AdminOrderDTO;
import admin.dto.OrderItemDTO;
import admin.dto.OrderStatisticsDTO;
import admin.dto.UserSubscriptionDTO;
import entity.enums.OrderStatus;
import entity.enums.OrderType;
import entity.order.Order;
import entity.order.OrderItem;
import entity.order.UserSubscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {

    private final OrderRepository orderRepository;

    /**
     * Отримати всі замовлення з пагінацією
     */
    public Page<AdminOrderDTO> getAllOrders(int page, int size) {
        log.debug("Getting all orders: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<Order> ordersPage = orderRepository.findAllWithDetails(pageable);

        return ordersPage.map(this::convertToDTO);
    }

    /**
     * Отримати замовлення з фільтрами
     */
    public Page<AdminOrderDTO> getOrdersWithFilters(
            OrderStatus status,
            OrderType type,
            LocalDateTime from,
            LocalDateTime to,
            String userEmail,
            int page,
            int size
    ) {
        log.debug("Getting orders with filters: status={}, type={}, from={}, to={}, email={}",
                status, type, from, to, userEmail);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<Order> ordersPage;

        // Якщо є пошук за email - використовуємо його
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            ordersPage = orderRepository.findByUserEmail(userEmail.trim(), pageable);
        }
        // Інакше - фільтрація за параметрами
        else {
            ordersPage = orderRepository.findWithFilters(status, type, from, to, pageable);
        }

        return ordersPage.map(this::convertToDTO);
    }

    /**
     * ВИПРАВЛЕНО: Отримати замовлення по ID з повними деталями
     * Використовуємо ДВА окремі запити щоб уникнути MultipleBagFetchException
     */
    public AdminOrderDTO getOrderById(Long id) {
        log.debug("Getting order by id: {}", id);

        // Крок 1: Завантажуємо Order з User, PromoCode (без колекцій)
        Order order = orderRepository.findByIdWithUser(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        // Крок 2: Окремо завантажуємо Items з деталями
        List<OrderItem> items = orderRepository.findItemsWithDetailsByOrderId(id);
        order.setItems(items);

        log.debug("Loaded order {} with {} items", id, items.size());

        // Крок 3: Якщо є subscription - завантажуємо його
        // Викликаємо getter щоб перевірити чи є subscription
        // Hibernate автоматично завантажить його в межах транзакції
        if (order.getSubscription() != null) {
            log.debug("Order has subscription: {}", order.getSubscription().getId());
        }

        return convertToDTO(order);
    }

    /**
     * Отримати статистику замовлень
     */
    public OrderStatisticsDTO getOrderStatistics() {
        log.debug("Calculating order statistics");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusWeeks(1);
        LocalDateTime monthStart = now.minusMonths(1);

        Long totalOrders = orderRepository.count();
        Long completedOrders = orderRepository.countByStatus(OrderStatus.COMPLETED);
        Long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        Long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);

        Long totalRevenue = orderRepository.getTotalRevenue();
        Double averageAmount = orderRepository.getAverageOrderAmount();

        Long singlePurchase = orderRepository.countByOrderType(OrderType.LESSON_PURCHASE);
        Long subscription = orderRepository.countByOrderType(OrderType.SUBSCRIPTION_PURCHASE);

        Long ordersToday = orderRepository.countOrdersSince(todayStart);
        Long ordersThisWeek = orderRepository.countOrdersSince(weekStart);
        Long ordersThisMonth = orderRepository.countOrdersSince(monthStart);

        // Конверсія
        Double conversionRate = totalOrders > 0
                ? (completedOrders.doubleValue() / totalOrders.doubleValue()) * 100
                : 0.0;

        return OrderStatisticsDTO.builder()
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .pendingOrders(pendingOrders)
                .cancelledOrders(cancelledOrders)
                .totalRevenue(totalRevenue)
                .averageOrderAmount(averageAmount)
                .singlePurchaseOrders(singlePurchase)
                .subscriptionOrders(subscription)
                .ordersToday(ordersToday)
                .ordersThisWeek(ordersThisWeek)
                .ordersThisMonth(ordersThisMonth)
                .conversionRate(conversionRate)
                .build();
    }

    // ========== ПРИВАТНІ МЕТОДИ КОНВЕРТАЦІЇ ==========

    /**
     * Конвертувати Order entity в AdminOrderDTO
     */
    private AdminOrderDTO convertToDTO(Order order) {
        // Отримати email користувача
        String userEmail = order.getUser().getPrimaryEmail();

        // Конвертувати items
        List<OrderItemDTO> itemDTOs = order.getItems() != null
                ? order.getItems().stream()
                .map(this::convertOrderItemToDTO)
                .collect(Collectors.toList())
                : new ArrayList<>();

        // Конвертувати підписку (якщо є)
        UserSubscriptionDTO subscriptionDTO = null;
        if (order.hasSubscription()) {
            subscriptionDTO = convertSubscriptionToDTO(order.getSubscription());
        }

        // Промокод
        Long discountAmount = null;
        if (order.getPromoCode() != null) {
            // TODO: Розрахувати суму знижки якщо потрібно
            discountAmount = 0L;
        }

        return AdminOrderDTO.builder()
                .id(order.getId())
                .status(order.getStatus())
                .orderType(order.getOrderType())
                .userId(order.getUser().getId())
                .userEmail(userEmail)
                .userName(order.getUser().getName())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .paymentGateway(order.getPaymentGateway())
                .promoCode(order.getPromoCode() != null ?
                        order.getPromoCode().getCode() : null)
                .discountAmount(discountAmount)
                .items(itemDTOs)
                .subscription(subscriptionDTO)
                .createdAt(order.getCreatedAt())
                .completedAt(order.getCompletedAt())
                .hasPromoCode(order.getPromoCode() != null)
                .hasSubscription(order.hasSubscription())
                .build();
    }

    private OrderItemDTO convertOrderItemToDTO(OrderItem item) {
        String lessonTitle = null;
        String planName = null;

        if (item.getLesson() != null) {
            // Отримати переклад уроку (українською)
            lessonTitle = item.getLesson().getTranslations().stream()
                    .filter(t -> "uk".equals(t.getLang()))
                    .findFirst()
                    .map(t -> t.getTitle())
                    .orElse("Урок #" + item.getLesson().getId());
        }

        if (item.getSubscriptionPlan() != null) {
            planName = item.getSubscriptionPlan().getNameForLang("uk");
        }

        return OrderItemDTO.builder()
                .id(item.getId())
                .lessonId(item.getLesson() != null ? item.getLesson().getId() : null)
                .lessonTitle(lessonTitle)
                .subscriptionPlanId(item.getSubscriptionPlan() != null ?
                        item.getSubscriptionPlan().getId() : null)
                .subscriptionPlanName(planName)
                .amount(item.getAmount())
                .currency(item.getCurrency())
                .build();
    }

    private UserSubscriptionDTO convertSubscriptionToDTO(UserSubscription sub) {
        return UserSubscriptionDTO.builder()
                .id(sub.getId())
                .planName(sub.getSubscriptionPlan().getNameForLang("uk"))
                .durationInDays(sub.getSubscriptionPlan().getDurationInDays())
                .status(sub.getStatus())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .autoRenew(sub.getAutoRenew())
                .cancelledAt(sub.getCancelledAt())
                .build();
    }
}