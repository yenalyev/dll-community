package admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatisticsDTO {

    // Загальна статистика
    private Long totalOrders;
    private Long completedOrders;
    private Long pendingOrders;
    private Long cancelledOrders;

    // Фінансова статистика (в копійках)
    private Long totalRevenue;
    private Double averageOrderAmount;

    // Статистика за типами
    private Long singlePurchaseOrders;
    private Long subscriptionOrders;

    // За період
    private Long ordersToday;
    private Long ordersThisWeek;
    private Long ordersThisMonth;

    // Конверсія
    private Double conversionRate; // (completed / total) * 100

    /**
     * Отримати дохід в основних одиницях
     */
    public Double getTotalRevenueInMainUnits() {
        return totalRevenue != null ? totalRevenue / 100.0 : 0.0;
    }

    /**
     * Отримати середній чек в основних одиницях
     */
    public Double getAverageOrderAmountInMainUnits() {
        return averageOrderAmount != null ? averageOrderAmount / 100.0 : 0.0;
    }
}