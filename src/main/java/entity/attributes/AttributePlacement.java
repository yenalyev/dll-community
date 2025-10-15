package entity.attributes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Довідник для можливих місць відображення атрибутів.
 * Наприклад: на картці товару, у фільтрах, прихований.
 */
@Entity
@Table(name = "attribute_placements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributePlacement {

    @Id
    // Для довідників часто використовують GenerationType.IDENTITY або заповнюють вручну
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "key", unique = true, nullable = false, length = 50)
    private String key;

    @Column(name = "description")
    private String description;
}
