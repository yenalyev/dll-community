package entity.order;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "subscription_plan_translation", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"plan_id", "lang"})
})
@Data
public class SubscriptionPlanTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(name = "lang", nullable = false, length = 5)
    private String lang;

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "description")
    private String description;
}
