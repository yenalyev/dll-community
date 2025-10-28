package entity.attributes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Тип розміщення атрибута (де він буде показуватися)
 * Наприклад: "product_card", "filters", "search"
 */
@Entity
@Table(name = "attribute_placements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttributePlacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Унікальний ключ розміщення (не змінюється після створення)
     * Наприклад: "product_card", "filters", "search"
     */
    @Column(name = "placement_key", unique = true, nullable = false, updatable = false)
    private String key;


    /**
     * Опис призначення цього розміщення
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Конструктор для створення нового placement з key
     * Використовується при створенні, оскільки key є immutable
     */
    public AttributePlacement(String key, String name, String description) {
        this.key = key;
        this.description = description;
    }

    /**
     * Builder pattern для зручності
     */
    public static AttributePlacementBuilder builder() {
        return new AttributePlacementBuilder();
    }

    public static class AttributePlacementBuilder {
        private String key;
        private String name;
        private String description;

        public AttributePlacementBuilder key(String key) {
            this.key = key;
            return this;
        }

        public AttributePlacementBuilder description(String description) {
            this.description = description;
            return this;
        }

        public AttributePlacement build() {
            return new AttributePlacement(key, name, description);
        }
    }
}
