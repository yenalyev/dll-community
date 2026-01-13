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

    // ⚠️ ЗМІНИТИ: Замість OneToOne зробити ManyToOne для історії підписок
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // Зв'язок з замовленням
    private Order order;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatus status;

    // ID підписки в платіжній системі (для авто-продовження)
    @Column(name = "gateway_subscription_id")
    private String gatewaySubscriptionId;

    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Метод для перевірки чи активна підписка
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE &&
                LocalDateTime.now().isBefore(endDate);
    }
}

