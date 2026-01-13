package entity.order;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "subscription_plan")
@Data
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "duration_in_days", nullable = false)
    private Integer durationInDays;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<SubscriptionPlanTranslation> translations = new HashSet<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<SubscriptionPlanPrice> prices = new HashSet<>();

    /**
     * Отримати переклад для мови
     */
    public SubscriptionPlanTranslation getTranslation(String lang) {
        return translations.stream()
                .filter(t -> t.getLang().equalsIgnoreCase(lang))
                .findFirst()
                .orElse(translations.stream()
                        .filter(t -> t.getLang().equalsIgnoreCase("uk"))
                        .findFirst()
                        .orElse(null));
    }


    /**
     * Отримати ціну для валюти
     */
    public SubscriptionPlanPrice getPrice(String currency) {
        return prices.stream()
                .filter(p -> p.getCurrency().name().equalsIgnoreCase(currency))
                .findFirst()
                .orElseGet(() -> prices.stream()
                        .findFirst()
                        .orElse(null));
    }

    public String getNameForLang(String lang) {
        SubscriptionPlanTranslation translation = getTranslation(lang);
        return translation != null ? translation.getName() : "План підписки";
    }

    public String getDescriptionForLang(String lang) {
        SubscriptionPlanTranslation translation = getTranslation(lang);
        return translation != null ? translation.getDescription() : "";
    }
}
