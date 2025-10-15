package entity.attributes;

import entity.lesson.Lesson;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Таблиця зв'язку, яка присвоює урокам конкретні значення атрибутів.
 */
@Entity
@Table(name = "lesson_attributes")
@Data
public class LessonAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

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
