package admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatisticsDTO {

    // Загальна статистика
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;

    // Підписки
    private Long usersWithActiveSubscriptions;
    private Long usersWithoutSubscriptions;

    // За період
    private Long newUsersToday;
    private Long newUsersThisWeek;
    private Long newUsersThisMonth;

    // За ролями
    private Long adminUsers;
    private Long managerUsers;
    private Long regularUsers;

    // За типом реєстрації
    private Long oauthUsers;
    private Long localUsers;

    // Retention
    private Double subscriptionRetentionRate; // % користувачів з активною підпискою
}