package admin.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class AttributeDto {

    private Long id;

    @NotBlank(message = "System name is required")
    private String name; // Системна назва (slug)

    @NotNull(message = "Type is required")
    private String type; // select, multiselect, text, number

    private Integer sortOrder;

    // Переклади для різних мов: key = langCode, value = label
    private Map<String, String> translations = new HashMap<>();

    // ID місць розміщення
    private List<Integer> placementIds = new ArrayList<>();

    // Опції для select/multiselect
    @Valid
    private List<AttributeOptionDto> options = new ArrayList<>();

    // Helper method для ініціалізації з entity
    public void addPlacementId(Integer id) {
        if (id != null && !placementIds.contains(id)) {
            placementIds.add(id);
        }
    }
}
