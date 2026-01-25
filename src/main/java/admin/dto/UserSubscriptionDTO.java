package admin.dto;

import entity.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserSubscriptionDTO {
    private Long id;
    private String planName;
    private Integer durationInDays;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean autoRenew;
    private LocalDateTime cancelledAt;

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE
                && LocalDateTime.now().isBefore(endDate);
    }

    public String getStatusText() {
        if (isActive()) return "Активна";
        switch (status) {
            case EXPIRED: return "Закінчилась";
            case CANCELED: return "Скасована";
            default: return status.name();
        }
    }
}