package entity.promo_codе;

import entity.enums.DiscountType;
import entity.lesson.Lesson;
import entity.order.SubscriptionPlan;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Зберігає інформацію про промокод та його загальні правила.
 */
@Entity
@Table(name = "promo_code")
@Data
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private Long discountValue; // Напр., 20 для 20% або 50000 для 500.00 грн

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "current_uses", nullable = false)
    private Integer currentUses = 0;

    @Column(name = "uses_per_user", nullable = false)
    private Integer usesPerUser = 1;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Зв'язок з історією використань
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "promoCode", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PromoCodeUsage> usages = new HashSet<>();

    // Зв'язок з уроками, на які діє промокод
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "promo_code_lesson",
            joinColumns = @JoinColumn(name = "promo_code_id"),
            inverseJoinColumns = @JoinColumn(name = "lesson_id")
    )
    private Set<Lesson> applicableLessons = new HashSet<>();

    // Зв'язок з планами підписок, на які діє промокод
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "promo_code_subscription_plan",
            joinColumns = @JoinColumn(name = "promo_code_id"),
            inverseJoinColumns = @JoinColumn(name = "subscription_plan_id")
    )
    private Set<SubscriptionPlan> applicablePlans = new HashSet<>();
}
