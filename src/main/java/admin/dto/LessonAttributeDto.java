package admin.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO для збереження значень атрибутів уроку
 */
@Data
public class LessonAttributeDto {

    private Long id; // ID запису в lesson_attributes (для оновлення)

    @NotNull(message = "Attribute ID is required")
    private Long attributeId;

    // Для select/multiselect - ID обраної опції
    private Long optionId;

    // Для text - текстове значення
    private String textValue;

    // Для number - числове значення
    private BigDecimal numberValue;

    // Допоміжні поля для відображення (не зберігаються)
    private String attributeName; // Системна назва атрибута
    private String attributeType; // Тип: select, multiselect, text, number
}
