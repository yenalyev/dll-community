package entity.attributes;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Зберігає всі можливі варіанти (опції) для атрибутів типу select та multiselect.
 */
@Entity
@Table(name = "attribute_options")
@Data
public class AttributeOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    @Column(name = "value", nullable = false)
    private String value; // Системне значення (slug), наприклад: a1

    @Column(name = "sort_order")
    private Integer sortOrder;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            mappedBy = "option",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OptionTranslation> translations = new ArrayList<>();
}
