package entity.attributes;

import lombok.Data;

import javax.persistence.*;

/**
 * Зберігає переклади назв атрибутів для різних мов.
 */
@Entity
@Table(name = "attribute_translations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"attribute_id", "lang_code"})
})
@Data
public class AttributeTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    @Column(name = "lang_code", nullable = false, length = 5)
    private String langCode;

    @Column(name = "label", nullable = false)
    private String label; // Назва для користувача, наприклад: "Рівень мови"
}

