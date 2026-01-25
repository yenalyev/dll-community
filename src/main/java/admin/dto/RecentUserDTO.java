package admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * DTO для відображення останніх користувачів на дашборді
 */
@Data
@Builder
public class RecentUserDTO {

    private Long userId;
    private String name;
    private String email;
    private LocalDateTime createdAt;

    /**
     * Отримати ініціали користувача для аватара
     */
    public String getInitials() {
        if (name == null || name.trim().isEmpty()) {
            return "??";
        }

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            // Якщо тільки одне слово - беремо перші 2 символи
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            // Перша літера імені + перша літера прізвища
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
    }

    /**
     * CSS клас для кольору аватара (випадковий на основі userId)
     */
    public String getAvatarColorClass() {
        if (userId == null) return "bg-gray-100 text-gray-600";

        String[] colors = {
                "bg-blue-100 text-blue-600",
                "bg-purple-100 text-purple-600",
                "bg-yellow-100 text-yellow-600",
                "bg-green-100 text-green-600",
                "bg-red-100 text-red-600",
                "bg-pink-100 text-pink-600",
                "bg-indigo-100 text-indigo-600"
        };

        int index = (int) (userId % colors.length);
        return colors[index];
    }

    /**
     * Час з моменту реєстрації у форматі "2 хв тому"
     */
    public String getRegisteredAgo() {
        if (createdAt == null) return "давно";

        LocalDateTime now = LocalDateTime.now();

        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        if (minutes < 1) return "щойно";
        if (minutes < 60) return minutes + " хв тому";

        long hours = ChronoUnit.HOURS.between(createdAt, now);
        if (hours < 24) return hours + " год тому";

        long days = ChronoUnit.DAYS.between(createdAt, now);
        if (days < 7) return days + " дн тому";

        long weeks = ChronoUnit.WEEKS.between(createdAt, now);
        if (weeks < 4) return weeks + " тиж тому";

        long months = ChronoUnit.MONTHS.between(createdAt, now);
        return months + " міс тому";
    }
}