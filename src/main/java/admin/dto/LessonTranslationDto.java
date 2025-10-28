package admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class LessonTranslationDto {

    private Long id;

    @NotBlank(message = "Language code is required")
    @Pattern(regexp = "uk|en|de", message = "Language must be uk, en, or de")
    private String lang;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 160, message = "Meta description must not exceed 160 characters")
    private String metaDescription;

    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    @Size(max = 100, message = "Slug must not exceed 100 characters")
    private String slug;
}
