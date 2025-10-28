package admin.dto;

import entity.lesson.Lesson;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;

@Data
public class LessonDto {

    private Long id;

    private String mainImageUrl;

    @NotNull(message = "Access level is required")
    private Lesson.AccessLevel accessLevel = Lesson.AccessLevel.PAID;

    // Переклади для трьох мов: key = langCode (uk, en, de)
    @Valid
    private Map<String, LessonTranslationDto> translations = new HashMap<>();

    // Ціни в різних валютах
    @Valid
    private List<LessonPriceDto> prices = new ArrayList<>();

    // Матеріали уроку
    @Valid
    private List<LessonMaterialDto> materials = new ArrayList<>();

    // Атрибути з їх значеннями
    @Valid
    private List<LessonAttributeDto> attributes = new ArrayList<>();

    // Дати створення/оновлення (тільки для відображення)
    private String createdAt;
    private String updatedAt;

    // Helper метод для ініціалізації перекладів для всіх мов
    public void initializeTranslations() {
        if (translations == null) {
            translations = new HashMap<>();
        }

        List<String> languages = Arrays.asList("uk", "en", "de");
        for (String lang : languages) {
            if (!translations.containsKey(lang)) {
                LessonTranslationDto translationDto = new LessonTranslationDto();
                translationDto.setLang(lang);
                translations.put(lang, translationDto);
            }
        }
    }
}
