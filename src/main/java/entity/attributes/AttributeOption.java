package entity.attributes;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Опція атрибута (для типів select/multiselect)
 */
@Entity
@Table(name = "attribute_options")
@Data
public class AttributeOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Атрибут, до якого належить опція
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    /**
     * Значення опції (ключ, наприклад: "beginner", "intermediate", "advanced")
     */
    @Column(name = "value", nullable = false)
    private String value;

    /**
     * Порядок сортування
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * Переклади для цієї опції
     * ВАЖЛИВО: List використовується, тому потрібно уникати MultipleBagFetchException
     * Використовуємо OptionTranslation замість AttributeOptionTranslation
     */
    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionTranslation> translations = new ArrayList<>();

    /**
     * Додати переклад
     */
    public void addTranslation(OptionTranslation translation) {
        translations.add(translation);
        translation.setOption(this);
    }

    /**
     * Видалити переклад
     */
    public void removeTranslation(OptionTranslation translation) {
        translations.remove(translation);
        translation.setOption(null);
    }
}
