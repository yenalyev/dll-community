package admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для відображення користувача в адмін-панелі
 */
@Data
@Builder
public class AdminUserDTO {

    // Основна інформація
    private Long id;
    private String name;
    private String primaryEmail;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // OAuth інформація
    private boolean isOAuthUser;
    private String authProvider; // LOCAL, GOOGLE, etc.

    // Статистика підписок
    private boolean hasActiveSubscription;
    private UserSubscriptionDTO activeSubscription;
    private Integer totalSubscriptions; // скільки разів купляв підписку

    // Статистика замовлень
    private Integer totalOrders;
    private Integer completedOrders;
    private Long totalSpent; // В копійках
    private Integer purchasedLessons; // Скільки уроків купив окремо

    // Активність
    private LocalDateTime lastOrderDate;
    private LocalDateTime lastLoginDate; // Якщо збираєте

    // Детальна інформація (для детальної сторінки)
    private List<String> allEmails;
    private UserSettingsDTO settings;

    /**
     * Отримати загальну суму витрат в основних одиницях
     */
    public Double getTotalSpentInMainUnits() {
        return totalSpent != null ? totalSpent / 100.0 : 0.0;
    }

    /**
     * Отримати кольоровий badge для статусу
     */
    public String getStatusBadgeClass() {
        if (!isActive) return "badge-danger";
        if (hasActiveSubscription) return "badge-success";
        return "badge-warning";
    }

    /**
     * Отримати текст статусу
     */
    public String getStatusText() {
        if (!isActive) return "Заблокований";
        if (hasActiveSubscription) return "Активна підписка";
        return "Без підписки";
    }
}