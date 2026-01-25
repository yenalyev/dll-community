package admin.dto;

import entity.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import static entity.enums.OrderStatus.*;

/**
 * DTO для відображення останніх замовлень на дашборді
 */
@Data
@Builder
public class RecentOrderDTO {

    private Long orderId;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private Long totalAmount;  // В копійках
    private OrderStatus status;
    private LocalDateTime createdAt;

    /**
     * Отримати суму в основних одиницях
     */
    public Double getTotalAmountInMainUnits() {
        return totalAmount != null ? totalAmount / 100.0 : 0.0;
    }

    /**
     * Форматована сума
     */
    public String getFormattedAmount() {
        return String.format("₴%.2f", getTotalAmountInMainUnits());
    }

    /**
     * Текст статусу українською
     */
    public String getStatusText() {
        if (status == null) return "Невідомо";

        switch (status) {
            case PENDING:
                return "Очікування";
            case PROCESSING:
                return "Обробка";
            case COMPLETED:
                return "Завершено";
            case FAILED:
                return "Помилка";
            case CANCELLED:
                return "Скасовано";
            case REFUNDED:
                return "Повернено";
            default:
                return status.name();
        }
    }

    /**
     * CSS клас для badge статусу
     */
    public String getStatusBadgeClass() {
        if (status == null) return "bg-gray-100 text-gray-700";

        switch (status) {
            case PENDING:
            case PROCESSING:
                return "bg-yellow-100 text-yellow-700";
            case COMPLETED:
                return "bg-green-100 text-green-700";
            case FAILED:
            case CANCELLED:
                return "bg-red-100 text-red-700";
            case REFUNDED:
                return "bg-blue-100 text-blue-700";
            default:
                return "bg-gray-100 text-gray-700";
        }
    }

    /**
     * Номер замовлення для відображення
     */
    public String getDisplayOrderNumber() {
        return "#" + (orderNumber != null ? orderNumber : orderId);
    }
}