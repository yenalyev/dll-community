package dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class TranslationDTO {
    private Long id;

    @NotBlank(message = "Мова обов'язкова")
    @Pattern(regexp = "ua|en|de", message = "Підтримуються мови: ua, en, de")
    private String lang;

    @NotBlank(message = "Назва обов'язкова")
    @Size(max = 255, message = "Назва не може бути довшою за 255 символів")
    private String name;

    private String description;
}
