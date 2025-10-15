package entity.attributes;

import lombok.Data;

import javax.persistence.*;

/**
 * Зберігає переклади назв опцій для різних мов.
 */
@Entity
@Table(name = "option_translations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"option_id", "lang_code"})
})
@Data
public class OptionTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private AttributeOption option;

    @Column(name = "lang_code", nullable = false, length = 5)
    private String langCode;

    @Column(name = "label", nullable = false)
    private String label; // Назва для користувача, наприклад: "Початковий (А1)"
}
