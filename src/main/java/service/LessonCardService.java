package service;

import dto.LessonCardDto;
import entity.attributes.LessonAttribute;
import entity.lesson.Lesson;
import entity.lesson.LessonTranslation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.LessonRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonCardService {

    private final LessonRepository lessonRepository;

    /**
     * Отримати картки всіх уроків для певної мови
     * Уроки сортуються за датою створення (найновіші спочатку)
     *
     * @param lang код мови (uk, en, de)
     * @return список DTO для карток уроків
     */
    public List<LessonCardDto> getAllLessonCards(String lang) {
        log.info("Getting all lesson cards for language: {}", lang);

        // Використовуємо метод з сортуванням за датою створення
        List<Lesson> lessons = lessonRepository.findAllWithTranslationsOrderByCreatedAtDesc();

        List<LessonCardDto> cards = new ArrayList<>();
        for (Lesson lesson : lessons) {
            try {
                LessonCardDto card = convertToCardDto(lesson, lang);
                if (card != null) {
                    cards.add(card);
                }
            } catch (Exception e) {
                log.error("Error converting lesson {} to card: {}", lesson.getId(), e.getMessage());
            }
        }

        log.info("Loaded {} lesson cards", cards.size());
        return cards;
    }

    /**
     * Отримати останні N карток уроків для певної мови
     *
     * @param lang код мови (uk, en, de)
     * @param limit максимальна кількість уроків
     * @return список DTO для карток уроків
     */
    public List<LessonCardDto> getLatestLessonCards(String lang, int limit) {
        log.info("Getting latest {} lesson cards for language: {}", limit, lang);

        return getAllLessonCards(lang).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Отримати картку одного уроку для певної мови
     *
     * @param lessonId ID уроку
     * @param lang код мови (uk, en, de)
     * @return DTO для картки уроку
     */
    public LessonCardDto getLessonCard(Long lessonId, String lang) {
        log.info("Getting lesson card for id={}, lang={}", lessonId, lang);

        Lesson lesson = lessonRepository.findByIdWithTranslations(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with id: " + lessonId));

        // Довантажуємо атрибути, якщо потрібно
        lessonRepository.findByIdWithAttributes(lessonId)
                .ifPresent(l -> lesson.setAttributes(l.getAttributes()));

        return convertToCardDto(lesson, lang);
    }

    /**
     * Конвертація Lesson Entity -> LessonCardDto
     *
     * @param lesson сутність уроку
     * @param lang код мови для перекладів
     * @return DTO для відображення картки
     */
    private LessonCardDto convertToCardDto(Lesson lesson, String lang) {
        LessonCardDto dto = new LessonCardDto();

        // Базові дані з Lesson
        dto.setId(lesson.getId());
        dto.setMainImageUrl(lesson.getMainImageUrl());
        dto.setAccessLevel(lesson.getAccessLevel() != null ? lesson.getAccessLevel().name() : "PAID");

        // Знаходимо переклад для потрібної мови
        LessonTranslation translation = lesson.getTranslations().stream()
                .filter(t -> t.getLang().equals(lang))
                .findFirst()
                .orElse(null);

        if (translation == null) {
            log.warn("No translation found for lesson {} in language {}", lesson.getId(), lang);
            return null;
        }

        dto.setTitle(translation.getTitle());
        dto.setDescription(translation.getDescription());
        dto.setSlug(translation.getSlug());

        // Обробка атрибутів
        if (lesson.getAttributes() != null && !lesson.getAttributes().isEmpty()) {
            // Розділяємо атрибути за placement
            List<String> cardHeaderAttributes = extractAttributesByPlacement(
                    lesson.getAttributes(), "product_card_head", lang
            );
            List<String> cardBodyAttributes = extractAttributesByPlacement(
                    lesson.getAttributes(), "product_card", lang
            );

            dto.setCardHeaderAttributes(cardHeaderAttributes);
            dto.setCardBodyAttributes(cardBodyAttributes);
        } else {
            dto.setCardHeaderAttributes(new ArrayList<>());
            dto.setCardBodyAttributes(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Витягує значення атрибутів для певного placement з перекладами
     *
     * @param lessonAttributes всі атрибути уроку
     * @param placementKey ключ placement (наприклад, "card_header", "card_body")
     * @param lang код мови для перекладів
     * @return список перекладених значень атрибутів
     */
    private List<String> extractAttributesByPlacement(
            Iterable<LessonAttribute> lessonAttributes,
            String placementKey,
            String lang) {

        List<String> result = new ArrayList<>();

        for (LessonAttribute lessonAttr : lessonAttributes) {
            // Перевіряємо, чи атрибут має потрібний placement
            boolean hasPlacement = lessonAttr.getAttribute().getPlacements().stream()
                    .anyMatch(p -> p.getKey().equals(placementKey));

            if (!hasPlacement) {
                continue;
            }

            // Отримуємо значення атрибута
            String value = getAttributeValue(lessonAttr, lang);
            if (value != null && !value.trim().isEmpty()) {
                result.add(value);
            }
        }

        return result;
    }

    /**
     * Отримує перекладене значення атрибута
     *
     * @param lessonAttribute атрибут уроку
     * @param lang код мови
     * @return перекладене значення або null
     */
    private String getAttributeValue(LessonAttribute lessonAttribute, String lang) {
        String attributeType = lessonAttribute.getAttribute().getType();

        switch (attributeType) {
            case "select":
            case "multiselect":
                // Для select/multiselect беремо переклад опції
                if (lessonAttribute.getOption() != null) {
                    return lessonAttribute.getOption().getTranslations().stream()
                            .filter(t -> t.getLangCode().equals(lang))
                            .findFirst()
                            .map(t -> t.getLabel())
                            .orElse(lessonAttribute.getOption().getValue());
                }
                break;

            case "text":
                // Для text повертаємо текстове значення
                return lessonAttribute.getTextValue();

            case "number":
                // Для number конвертуємо в рядок
                if (lessonAttribute.getNumberValue() != null) {
                    return lessonAttribute.getNumberValue().toString();
                }
                break;

            default:
                log.warn("Unknown attribute type: {}", attributeType);
        }

        return null;
    }
}
