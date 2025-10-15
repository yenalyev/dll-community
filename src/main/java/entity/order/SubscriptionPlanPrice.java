package entity.order;

import entity.enums.Currency;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "subscription_plan_price", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"plan_id", "currency"})
})
@Data
public class SubscriptionPlanPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Column(name = "amount", nullable = false)
    private Long amount; // Сума в копійках/центах
}
