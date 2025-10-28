package admin.dto;

import entity.lesson.LessonMaterial;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class LessonMaterialDto {

    private Long id;

    @NotNull(message = "Material type is required")
    private LessonMaterial.MaterialType materialType;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content; // URL для PDF/LINK/IMAGE або текст для TEXT_BLOCK

    @NotNull(message = "Sort order is required")
    private Integer sortOrder = 0;
}
