package admin.service;

import admin.dto.*;
import entity.attributes.Attribute;
import entity.attributes.LessonAttribute;
import entity.lesson.*;
import repository.AttributeRepository;
import repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final AttributeRepository attributeRepository;

    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList("uk", "en", "de");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ========== ОСНОВНІ CRUD МЕТОДИ ==========

    /**
     * Отримати урок за ID з усіма деталями
     */
    public LessonDto getLessonById(Long id) {
        log.info("Getting lesson by id: {}", id);

        Lesson lesson = findLessonWithAllDetails(id);
        return convertToDto(lesson);
    }

    /**
     * Отримати всі уроки (легка версія - тільки з перекладами)
     */
    public List<LessonDto> getAllLessons() {
        log.info("Getting all lessons");

        List<Lesson> lessons = lessonRepository.findAllWithTranslations();

        return lessons.stream()
                .map(this::convertToLightDto)
                .collect(Collectors.toList());
    }

    /**
     * Створити новий урок
     */
    @Transactional
    public LessonDto createLesson(LessonDto dto) {
        log.info("Creating new lesson");

        // Валідація
        validateLessonDto(dto);
        validateSlugsUniqueness(dto, null);

        // Створення нової сутності
        Lesson lesson = new Lesson();
        mapDtoToEntity(dto, lesson);

        // Встановлення дат
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());

        // Збереження
        Lesson saved = lessonRepository.save(lesson);
        log.info("Lesson created successfully with id: {}", saved.getId());

        return convertToDto(saved);
    }

    /**
     * Оновити існуючий урок
     */
    @Transactional
    public LessonDto updateLesson(Long id, LessonDto dto) {
        log.info("Updating lesson: id={}", id);

        // Завантажуємо урок з усіма деталями
        Lesson lesson = findLessonWithAllDetails(id);

        // Валідація
        validateLessonDto(dto);
        validateSlugsUniqueness(dto, id);

        // Оновлення даних
        mapDtoToEntity(dto, lesson);

        // Оновлення дати
        lesson.setUpdatedAt(LocalDateTime.now());

        // Збереження
        Lesson updated = lessonRepository.save(lesson);
        log.info("Lesson updated successfully: id={}", updated.getId());

        return convertToDto(updated);
    }

    /**
     * Видалити урок
     */
    @Transactional
    public void deleteLesson(Long id) {
        log.info("Deleting lesson: id={}", id);

        if (!lessonRepository.existsById(id)) {
            throw new IllegalArgumentException("Lesson not found with id: " + id);
        }

        // Можна додати перевірку на використання в замовленнях

        lessonRepository.deleteById(id);
        log.info("Lesson deleted successfully: id={}", id);
    }

    // ========== ДОПОМІЖНІ МЕТОДИ - ЗАВАНТАЖЕННЯ ==========

    /**
     * Завантажити урок з усіма деталями
     */
    private Lesson findLessonWithAllDetails(Long id) {
        log.debug("Loading lesson with all details: id={}", id);

        // Крок 1: Завантажуємо урок з перекладами
        Lesson lesson = lessonRepository.findByIdWithTranslations(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with id: " + id));

        // Крок 2: Довантажуємо ціни
        lessonRepository.findByIdWithPrices(id).ifPresent(l ->
                lesson.setPrices(l.getPrices())
        );

        // Крок 3: Довантажуємо матеріали
        lessonRepository.findByIdWithMaterials(id).ifPresent(l ->
                lesson.setMaterials(l.getMaterials())
        );

        // Крок 4: Довантажуємо атрибути
        lessonRepository.findByIdWithAttributes(id).ifPresent(l ->
                lesson.setAttributes(l.getAttributes())
        );

        log.debug("Lesson loaded: {} translations, {} prices, {} materials, {} attributes",
                lesson.getTranslations() != null ? lesson.getTranslations().size() : 0,
                lesson.getPrices() != null ? lesson.getPrices().size() : 0,
                lesson.getMaterials() != null ? lesson.getMaterials().size() : 0,
                lesson.getAttributes() != null ? lesson.getAttributes().size() : 0);

        return lesson;
    }

    // ========== ДОПОМІЖНІ МЕТОДИ - ВАЛІДАЦІЯ ==========

    /**
     * Валідація DTO уроку
     */
    private void validateLessonDto(LessonDto dto) {
        if (dto.getAccessLevel() == null) {
            throw new IllegalArgumentException("Access level is required");
        }

        // Перевірка наявності перекладів для всіх мов
        for (String lang : SUPPORTED_LANGUAGES) {
            if (!dto.getTranslations().containsKey(lang) ||
                    dto.getTranslations().get(lang) == null) {
                throw new IllegalArgumentException("Translation for language '" + lang + "' is required");
            }

            LessonTranslationDto translation = dto.getTranslations().get(lang);
            if (translation.getTitle() == null || translation.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Title for language '" + lang + "' is required");
            }
            if (translation.getSlug() == null || translation.getSlug().trim().isEmpty()) {
                throw new IllegalArgumentException("Slug for language '" + lang + "' is required");
            }
        }

        // Для PAID уроків потрібна хоча б одна ціна
        if (dto.getAccessLevel() == Lesson.AccessLevel.PAID) {
            if (dto.getPrices() == null || dto.getPrices().isEmpty()) {
                throw new IllegalArgumentException("At least one price is required for paid lessons");
            }
        }
    }

    /**
     * Перевірка унікальності slug для всіх мов
     */
    private void validateSlugsUniqueness(LessonDto dto, Long lessonId) {
        for (Map.Entry<String, LessonTranslationDto> entry : dto.getTranslations().entrySet()) {
            String lang = entry.getKey();
            String slug = entry.getValue().getSlug();

            boolean exists;
            if (lessonId != null) {
                // Оновлення - виключаємо поточний урок
                exists = lessonRepository.existsByLangAndSlugExcludingLesson(lang, slug, lessonId);
            } else {
                // Створення
                exists = lessonRepository.existsByLangAndSlug(lang, slug);
            }

            if (exists) {
                throw new IllegalArgumentException(
                        "Slug '" + slug + "' already exists for language '" + lang + "'"
                );
            }
        }
    }

    // ========== ДОПОМІЖНІ МЕТОДИ - КОНВЕРТАЦІЯ ==========

    /**
     * Конвертація Entity -> DTO (легка версія)
     */
    private LessonDto convertToLightDto(Lesson lesson) {
        LessonDto dto = new LessonDto();
        dto.setId(lesson.getId());
        dto.setMainImageUrl(lesson.getMainImageUrl());
        dto.setAccessLevel(lesson.getAccessLevel());

        // Переклади
        if (lesson.getTranslations() != null) {
            Map<String, LessonTranslationDto> translations = new HashMap<>();
            for (LessonTranslation translation : lesson.getTranslations()) {
                translations.put(translation.getLang(), convertTranslationToDto(translation));
            }
            dto.setTranslations(translations);
        }

        // Дати
        if (lesson.getCreatedAt() != null) {
            dto.setCreatedAt(lesson.getCreatedAt().format(DATE_FORMATTER));
        }
        if (lesson.getUpdatedAt() != null) {
            dto.setUpdatedAt(lesson.getUpdatedAt().format(DATE_FORMATTER));
        }

        return dto;
    }

    /**
     * Конвертація Entity -> DTO (повна версія)
     */
    private LessonDto convertToDto(Lesson lesson) {
        LessonDto dto = convertToLightDto(lesson);

        // Ціни
        if (lesson.getPrices() != null) {
            List<LessonPriceDto> prices = new ArrayList<>();
            for (LessonPrice price : lesson.getPrices()) {
                prices.add(convertPriceToDto(price));
            }
            dto.setPrices(prices);
        }

        // Матеріали
        if (lesson.getMaterials() != null) {
            List<LessonMaterialDto> materials = new ArrayList<>();
            for (LessonMaterial material : lesson.getMaterials()) {
                materials.add(convertMaterialToDto(material));
            }
            materials.sort(Comparator.comparing(LessonMaterialDto::getSortOrder));
            dto.setMaterials(materials);
        }

        // Атрибути
        if (lesson.getAttributes() != null) {
            List<Long> attributeIds = lesson.getAttributes().stream()
                    .map(la -> la.getAttribute().getId())
                    .collect(Collectors.toList());
            dto.setAttributeIds(attributeIds);
        }

        return dto;
    }

    /**
     * Мапінг DTO -> Entity
     */
    private void mapDtoToEntity(LessonDto dto, Lesson lesson) {
        lesson.setMainImageUrl(dto.getMainImageUrl());
        lesson.setAccessLevel(dto.getAccessLevel());

        // Оновлення перекладів
        updateTranslations(lesson, dto.getTranslations());

        // Оновлення цін
        updatePrices(lesson, dto.getPrices());

        // Оновлення матеріалів
        updateMaterials(lesson, dto.getMaterials());

        // Оновлення атрибутів
        updateAttributes(lesson, dto.getAttributeIds());
    }

    /**
     * Оновлення перекладів
     */
    private void updateTranslations(Lesson lesson, Map<String, LessonTranslationDto> translationsMap) {
        if (lesson.getTranslations() == null) {
            lesson.setTranslations(new HashSet<>());
        }

        // Видаляємо переклади, яких немає в DTO
        lesson.getTranslations().removeIf(translation ->
                !translationsMap.containsKey(translation.getLang())
        );

        // Додаємо або оновлюємо переклади
        for (Map.Entry<String, LessonTranslationDto> entry : translationsMap.entrySet()) {
            String lang = entry.getKey();
            LessonTranslationDto dto = entry.getValue();

            Optional<LessonTranslation> existing = lesson.getTranslations().stream()
                    .filter(t -> t.getLang().equals(lang))
                    .findFirst();

            if (existing.isPresent()) {
                // Оновлюємо існуючий
                updateTranslation(existing.get(), dto);
            } else {
                // Створюємо новий
                LessonTranslation translation = new LessonTranslation();
                translation.setLesson(lesson);
                translation.setLang(lang);
                updateTranslation(translation, dto);
                lesson.getTranslations().add(translation);
            }
        }
    }

    private void updateTranslation(LessonTranslation translation, LessonTranslationDto dto) {
        translation.setTitle(dto.getTitle());
        translation.setDescription(dto.getDescription());
        translation.setMetaDescription(dto.getMetaDescription());
        translation.setSlug(dto.getSlug());
    }

    /**
     * Оновлення цін
     */
    private void updatePrices(Lesson lesson, List<LessonPriceDto> priceDtos) {
        if (lesson.getPrices() == null) {
            lesson.setPrices(new HashSet<>());
        }

        // Збираємо ID цін з DTO
        Set<Long> dtoPriceIds = priceDtos.stream()
                .map(LessonPriceDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Видаляємо ціни, яких немає в DTO
        lesson.getPrices().removeIf(price ->
                price.getId() != null && !dtoPriceIds.contains(price.getId())
        );

        // Додаємо або оновлюємо ціни
        for (LessonPriceDto dto : priceDtos) {
            if (dto.getId() != null) {
                // Оновлюємо існуючу
                Optional<LessonPrice> existing = lesson.getPrices().stream()
                        .filter(p -> dto.getId().equals(p.getId()))
                        .findFirst();

                if (existing.isPresent()) {
                    updatePrice(existing.get(), dto);
                }
            } else {
                // Створюємо нову
                LessonPrice price = new LessonPrice();
                price.setLesson(lesson);
                updatePrice(price, dto);
                lesson.getPrices().add(price);
            }
        }
    }

    private void updatePrice(LessonPrice price, LessonPriceDto dto) {
        price.setCurrency(dto.getCurrency());
        price.setAmount(dto.getAmount());
    }

    /**
     * Оновлення матеріалів
     */
    private void updateMaterials(Lesson lesson, List<LessonMaterialDto> materialDtos) {
        if (lesson.getMaterials() == null) {
            lesson.setMaterials(new HashSet<>());
        }

        // Збираємо ID матеріалів з DTO
        Set<Long> dtoMaterialIds = materialDtos.stream()
                .map(LessonMaterialDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Видаляємо матеріали, яких немає в DTO
        lesson.getMaterials().removeIf(material ->
                material.getId() != null && !dtoMaterialIds.contains(material.getId())
        );

        // Додаємо або оновлюємо матеріали
        for (LessonMaterialDto dto : materialDtos) {
            if (dto.getId() != null) {
                // Оновлюємо існуючий
                Optional<LessonMaterial> existing = lesson.getMaterials().stream()
                        .filter(m -> dto.getId().equals(m.getId()))
                        .findFirst();

                if (existing.isPresent()) {
                    updateMaterial(existing.get(), dto);
                }
            } else {
                // Створюємо новий
                LessonMaterial material = new LessonMaterial();
                material.setLesson(lesson);
                updateMaterial(material, dto);
                lesson.getMaterials().add(material);
            }
        }
    }

    private void updateMaterial(LessonMaterial material, LessonMaterialDto dto) {
        material.setMaterialType(dto.getMaterialType());
        material.setTitle(dto.getTitle());
        material.setContent(dto.getContent());
        material.setSortOrder(dto.getSortOrder());
    }

    /**
     * Оновлення атрибутів
     */
    private void updateAttributes(Lesson lesson, List<Long> attributeIds) {
        if (lesson.getAttributes() == null) {
            lesson.setAttributes(new HashSet<>());
        }

        // Очищуємо старі атрибути
        lesson.getAttributes().clear();

        // Додаємо нові
        if (attributeIds != null && !attributeIds.isEmpty()) {
            for (Long attributeId : attributeIds) {
                attributeRepository.findById(attributeId).ifPresent(attribute -> {
                    LessonAttribute lessonAttribute = new LessonAttribute();
                    lessonAttribute.setLesson(lesson);
                    lessonAttribute.setAttribute(attribute);
                    lesson.getAttributes().add(lessonAttribute);
                });
            }
        }
    }

    // ========== КОНВЕРТЕРИ ==========

    private LessonTranslationDto convertTranslationToDto(LessonTranslation translation) {
        LessonTranslationDto dto = new LessonTranslationDto();
        dto.setId(translation.getId());
        dto.setLang(translation.getLang());
        dto.setTitle(translation.getTitle());
        dto.setDescription(translation.getDescription());
        dto.setMetaDescription(translation.getMetaDescription());
        dto.setSlug(translation.getSlug());
        return dto;
    }

    private LessonPriceDto convertPriceToDto(LessonPrice price) {
        LessonPriceDto dto = new LessonPriceDto();
        dto.setId(price.getId());
        dto.setCurrency(price.getCurrency());
        dto.setAmount(price.getAmount());
        return dto;
    }

    private LessonMaterialDto convertMaterialToDto(LessonMaterial material) {
        LessonMaterialDto dto = new LessonMaterialDto();
        dto.setId(material.getId());
        dto.setMaterialType(material.getMaterialType());
        dto.setTitle(material.getTitle());
        dto.setContent(material.getContent());
        dto.setSortOrder(material.getSortOrder());
        return dto;
    }
}
