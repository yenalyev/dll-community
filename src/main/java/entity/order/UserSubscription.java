package entity.order;

import entity.enums.SubscriptionStatus;

import entity.user.User;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscription", indexes = {
        @Index(name = "idx_user_status", columnList = "user_id, status"),
        @Index(name = "idx_end_date", columnList = "end_date")
})
@Data
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "gateway_subscription_id")
    private String gatewaySubscriptionId;

    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ⬇️ НОВЕ ПОЛЕ
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now; // ⬅️ ініціалізуємо при створенні
    }

    // ⬇️ НОВИЙ МЕТОД
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== ДОПОМІЖНІ МЕТОДИ ==========

    public boolean isActive() {
        if (status != SubscriptionStatus.ACTIVE) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate) && now.isBefore(endDate);
    }

    public boolean isCancelled() {
        return cancelledAt != null;
    }

    public boolean willAutoRenew() {
        return autoRenew && !isCancelled() && status == SubscriptionStatus.ACTIVE;
    }

    /**
     * Скасувати автопродовження
     * updatedAt оновиться автоматично через @PreUpdate
     */
    public void cancelAutoRenew() {
        this.autoRenew = false;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * Продовжити підписку на новий період
     * updatedAt оновиться автоматично через @PreUpdate
     */
    public void renew(LocalDateTime newEndDate, LocalDateTime newNextBillingDate) {
        this.startDate = this.endDate;
        this.endDate = newEndDate;
        this.nextBillingDate = newNextBillingDate;
        this.status = SubscriptionStatus.ACTIVE;
        this.cancelledAt = null;
    }
}