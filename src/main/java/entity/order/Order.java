package entity.order;

import entity.enums.Currency;
import entity.enums.OrderStatus;
import entity.enums.OrderType;
import entity.promo_codе.PromoCode;
import entity.user.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Column(name = "payment_gateway", length = 50)
    private String paymentGateway;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_code_id")
    private PromoCode promoCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // Зв'язок з підпискою (якщо це замовлення на підписку)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<UserSubscription> subscriptions = new ArrayList<>();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ===== ДОДАТИ ЦЕЙ HELPER METHOD =====
    /**
     * Отримати першу (основну) підписку з замовлення.
     * Зазвичай в замовленні одна підписка, але технічно може бути список.
     */
    @Transient
    public UserSubscription getSubscription() {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return null;
        }
        return subscriptions.get(0);
    }

    /**
     * Перевірити чи є підписка в замовленні
     */
    @Transient
    public boolean hasSubscription() {
        return subscriptions != null && !subscriptions.isEmpty();
    }
}