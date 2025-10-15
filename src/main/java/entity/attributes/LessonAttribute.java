package entity.attributes;

import lombok.Data;
// Уявімо, що у вас є сутність Lesson в пакеті com.dllcommunity.platform.entity.lesson
// import com.dllcommunity.platform.entity.lesson.Lesson;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Таблиця зв'язку, яка присвоює урокам (товарам) конкретні значення атрибутів.
 */
@Entity
@Table(name = "lesson_attributes")
@Data
public class LessonAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Замініть це на реальний зв'язок @ManyToOne, коли сутність Lesson буде готова
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "lesson_id", nullable = false)
    // private Lesson lesson;
    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id") // Може бути NULL
    private AttributeOption option;

    @Lob // Для довгих текстових значень
    @Column(name = "text_value")
    private String textValue;

    @Column(name = "number_value", precision = 10, scale = 2)
    private BigDecimal numberValue;
}
