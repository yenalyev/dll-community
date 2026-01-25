package admin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO для статистики на дашборді адмін-панелі
 */
@Data
@Builder
public class DashboardStatsDTO {

    // Users статистика
    private Long totalUsers;
    private Double usersGrowthPercent;

    // Lessons статистика
    private Long totalLessons;
    private Double lessonsGrowthPercent;

    // Orders статистика
    private Long totalOrders;
    private Double ordersGrowthPercent;

    // Revenue статистика
    private Long totalRevenue;  // В копійках
    private Double revenueGrowthPercent;

    /**
     * Отримати дохід в основних одиницях (гривні)
     */
    public Double getTotalRevenueInMainUnits() {
        return totalRevenue != null ? totalRevenue / 100.0 : 0.0;
    }

    /**
     * Форматований дохід для відображення
     */
    public String getFormattedRevenue() {
        return String.format("₴%.2f", getTotalRevenueInMainUnits());
    }

    /**
     * Форматований відсоток зростання користувачів
     */
    public String getFormattedUsersGrowth() {
        if (usersGrowthPercent == null) return "+0%";
        return String.format("%+.0f%%", usersGrowthPercent);
    }

    /**
     * Форматований відсоток зростання уроків
     */
    public String getFormattedLessonsGrowth() {
        if (lessonsGrowthPercent == null) return "+0%";
        return String.format("%+.0f%%", lessonsGrowthPercent);
    }

    /**
     * Форматований відсоток зростання замовлень
     */
    public String getFormattedOrdersGrowth() {
        if (ordersGrowthPercent == null) return "+0%";
        return String.format("%+.0f%%", ordersGrowthPercent);
    }

    /**
     * Форматований відсоток зростання доходу
     */
    public String getFormattedRevenueGrowth() {
        if (revenueGrowthPercent == null) return "+0%";
        return String.format("%+.0f%%", revenueGrowthPercent);
    }

    /**
     * CSS клас для badge зростання користувачів
     */
    public String getUsersGrowthBadgeClass() {
        return getGrowthBadgeClass(usersGrowthPercent);
    }

    /**
     * CSS клас для badge зростання уроків
     */
    public String getLessonsGrowthBadgeClass() {
        return getGrowthBadgeClass(lessonsGrowthPercent);
    }

    /**
     * CSS клас для badge зростання замовлень
     */
    public String getOrdersGrowthBadgeClass() {
        return getGrowthBadgeClass(ordersGrowthPercent);
    }

    /**
     * CSS клас для badge зростання доходу
     */
    public String getRevenueGrowthBadgeClass() {
        return getGrowthBadgeClass(revenueGrowthPercent);
    }

    /**
     * Допоміжний метод для визначення кольору badge
     */
    private String getGrowthBadgeClass(Double percent) {
        if (percent == null || percent == 0) {
            return "text-gray-600 bg-gray-50";
        } else if (percent > 0) {
            return "text-green-600 bg-green-50";
        } else {
            return "text-red-600 bg-red-50";
        }
    }
}