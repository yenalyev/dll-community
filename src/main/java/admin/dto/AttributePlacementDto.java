package admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributePlacementDto {

    private Integer id;

    @NotBlank(message = "Key is required")
    private String key;

    private String description;
}
