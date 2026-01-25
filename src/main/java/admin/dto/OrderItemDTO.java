package admin.dto;

import entity.enums.Currency;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemDTO {
    private Long id;

    // Урок (якщо це покупка уроку)
    private Long lessonId;
    private String lessonTitle;

    // План підписки (якщо це підписка)
    private Long subscriptionPlanId;
    private String subscriptionPlanName;

    // Фінанси
    private Long amount;
    private Currency currency;

    public Double getAmountInMainUnits() {
        return amount != null ? amount / 100.0 : 0.0;
    }

    public String getProductName() {
        if (lessonTitle != null) return lessonTitle;
        if (subscriptionPlanName != null) return subscriptionPlanName;
        return "Невідомий продукт";
    }
}