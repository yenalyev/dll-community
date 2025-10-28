package dto;

import lombok.Data;

import java.util.List;

@Data
public class LessonCardDto {
    /** ID уроку (з Lesson) */
    private Long id;

    /** URL головного зображення (з Lesson) */
    private String mainImageUrl;

    /** Назва уроку (вже перекладена, з LessonTranslation) */
    private String title;

    /** Короткий опис (вже перекладений, з LessonTranslation) */
    private String description;

    /** Slug для URL (вже перекладений, з LessonTranslation) */
    private String slug;

    /** * Рівень доступу (з Lesson.accessLevel)
     * Використовуємо String для простоти у Thymeleaf.
     * Буде "FREE" або "PAID".
     */
    private String accessLevel;

    /** * Рівень складності (вже перекладений, напр. "C1", "B2")
     * (з LessonAttribute -> AttributeOption -> OptionTranslation)
     */
    private List<String> cardHeaderAttributes;

    /** * Категорія/тип активності (вже перекладена, напр. "Speaking activity")
     * (з LessonAttribute -> AttributeOption -> OptionTranslation)
     */
    private List<String> cardBodyAttributes;

    // ==================================================================
    // КАСТОМНИЙ ГЕТТЕР
    // ==================================================================

    /**
     * Повертає відформатований рівень доступу для відображення.
     * Lombok @Data не буде генерувати цей метод, оскільки він визначений вручну.
     *
     * @return "Free", "DLL Pro", або оригінальне значення.
     */
    public String getAccessLevel() {
        // Перевірка на null, щоб уникнути NullPointerException
        if (this.accessLevel == null) {
            return null;
        }

        // Використовуємо .equals() для порівняння рядків
        if ("FREE".equals(this.accessLevel)) {
            return "Free";
        }

        if ("PAID".equals(this.accessLevel)) {
            return "DLL Pro";
        }

        // Повертаємо оригінальне значення, якщо воно не FREE/PAID
        return this.accessLevel;
    }
}
