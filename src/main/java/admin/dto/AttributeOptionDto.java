package admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@Data
public class AttributeOptionDto {

    private Long id;

    @NotBlank(message = "Option value is required")
    private String value; // Системне значення (slug)

    private Integer sortOrder;

    // Переклади для різних мов: key = langCode, value = label
    private Map<String, String> translations = new HashMap<>();
}
