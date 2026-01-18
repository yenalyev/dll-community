package entity.order;

import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscription_plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Унікальний системний ключ
     * monthly, semiannual, annual
     */
    @Column(name = "plan_key", unique = true, nullable = false, length = 50)
    private String planKey;

    /**
     * Тривалість підписки в днях
     */
    @Column(name = "duration_in_days", nullable = false)
    private Integer durationInDays;

    /**
     * Чи план активний (доступний для покупки)
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Порядок відображення (для сортування в UI)
     */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private List<SubscriptionPlanTranslation> translations = new ArrayList<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private List<SubscriptionPlanPrice> prices = new ArrayList<>();

    // ========== ДОПОМІЖНІ МЕТОДИ ==========

    /**
     * Отримати ціну для певної валюти
     */
    public SubscriptionPlanPrice getPriceForCurrency(entity.enums.Currency currency) {
        return prices.stream()
                .filter(p -> p.getCurrency() == currency)
                .findFirst()
                .orElse(null);
    }

    /**
     * Отримати переклад для певної мови
     */
    public SubscriptionPlanTranslation getTranslation(String lang) {
        return translations.stream()
                .filter(t -> t.getLang().equals(lang))
                .findFirst()
                .orElse(null);
    }

    /**
     * Отримати назву плану для певної мови
     *
     * @param lang код мови (uk, en, de)
     * @return назва плану або ключ плану якщо переклад не знайдено
     */
    public String getNameForLang(String lang) {
        SubscriptionPlanTranslation translation = getTranslation(lang);
        return translation != null ? translation.getName() : planKey;
    }

    /**
     * Отримати опис плану для певної мови
     *
     * @param lang код мови (uk, en, de)
     * @return опис плану або null якщо переклад не знайдено
     */
    public String getDescriptionForLang(String lang) {
        SubscriptionPlanTranslation translation = getTranslation(lang);
        return translation != null ? translation.getDescription() : null;
    }
}