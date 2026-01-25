package admin.dto;

import entity.enums.Currency;
import entity.enums.OrderStatus;
import entity.enums.OrderType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для відображення замовлення в адмін-панелі
 */
@Data
@Builder
public class AdminOrderDTO {

    // Основна інформація
    private Long id;
    private OrderStatus status;
    private OrderType orderType;

    // Користувач
    private Long userId;
    private String userEmail;
    private String userName;

    // Фінанси
    private Long totalAmount;
    private Currency currency;
    private String paymentGateway;

    // Промокод (якщо був)
    private String promoCode;
    private Long discountAmount;

    // Items замовлення
    private List<OrderItemDTO> items;

    // Підписка (якщо це замовлення на підписку)
    private UserSubscriptionDTO subscription;

    // Дати
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // Додаткові дані
    private boolean hasPromoCode;
    private boolean hasSubscription;

    /**
     * Отримати суму в основних одиницях (грн/євро)
     */
    public Double getTotalAmountInMainUnits() {
        return totalAmount != null ? totalAmount / 100.0 : 0.0;
    }

    /**
     * Отримати статус у вигляді тексту
     */
    public String getStatusText() {
        switch (status) {
            case PENDING: return "Очікує оплати";
            case COMPLETED: return "Завершено";
            case CANCELLED: return "Скасовано";
            default: return status.name();
        }
    }

    /**
     * Отримати тип замовлення у вигляді тексту
     */
    public String getOrderTypeText() {
        switch (orderType) {
            case LESSON_PURCHASE: return "Покупка уроку";
            case SUBSCRIPTION_PURCHASE: return "Підписка";
            default: return orderType.name();
        }
    }
}