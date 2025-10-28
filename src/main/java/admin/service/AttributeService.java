package admin.service;

import admin.dto.AttributeDto;
import admin.dto.AttributeOptionDto;
import admin.dto.AttributePlacementDto;
import entity.attributes.Attribute;
import entity.attributes.AttributeOption;
import entity.attributes.AttributePlacement;
import entity.attributes.AttributeTranslation;
import entity.attributes.OptionTranslation;
import repository.AttributeRepository;
import repository.AttributePlacementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttributeService {

    private final AttributeRepository attributeRepository;
    private final AttributePlacementRepository placementRepository;

    // ========== ОСНОВНІ МЕТОДИ ДЛЯ АТРИБУТІВ ==========

    /**
     * Завантажити атрибут з усіма деталями (переклади + опції + переклади опцій)
     * Використовуємо окремі запити, щоб уникнути MultipleBagFetchException
     */
    private Attribute findAttributeWithAllDetails(Long id) {
        log.debug("Loading attribute with all details: id={}", id);

        // Крок 1: Завантажуємо атрибут з його перекладами
        Attribute attribute = attributeRepository.findByIdWithTranslations(id)
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found with id: " + id));

        // Крок 2: Довантажуємо опції (без їх перекладів)
        Attribute attrWithOptions = attributeRepository.findByIdWithOptions(id)
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found with id: " + id));

        // Копіюємо опції з другого запиту в наш основний об'єкт
        attribute.setOptions(attrWithOptions.getOptions());

        // Крок 3: Для кожної опції довантажуємо її переклади
        if (attribute.getOptions() != null && !attribute.getOptions().isEmpty()) {
            for (AttributeOption option : attribute.getOptions()) {
                if (option.getId() != null) {
                    // Завантажуємо опцію з перекладами
                    AttributeOption optionWithTranslations = attributeRepository.findOptionByIdWithTranslations(option.getId())
                            .orElse(option);
                    // Встановлюємо переклади
                    option.setTranslations(optionWithTranslations.getTranslations());
                }
            }
        }

        log.debug("Attribute loaded successfully with {} options",
                attribute.getOptions() != null ? attribute.getOptions().size() : 0);

        return attribute;
    }

    /**
     * Отримати атрибут за ID з усіма деталями
     */
    public AttributeDto getAttributeById(Long id) {
        log.info("Getting attribute by id: {}", id);

        Attribute attribute = findAttributeWithAllDetails(id);
        AttributeDto dto = convertToDtoWithOptions(attribute);

        log.debug("Converted attribute to DTO with {} options",
                dto.getOptions() != null ? dto.getOptions().size() : 0);

        return dto;
    }

    /**
     * Отримати всі атрибути (легка версія - тільки з перекладами, без опцій)
     * Використовується для списку на головній сторінці
     */
    public List<AttributeDto> getAllAttributes() {
        log.info("Getting all attributes (light version)");

        List<Attribute> attributes = attributeRepository.findAllWithTranslations();

        return attributes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Отримати всі атрибути з повними деталями (з опціями та їх перекладами)
     * Використовується коли потрібна повна інформація
     */
    public List<AttributeDto> getAllAttributesWithDetails() {
        log.info("Getting all attributes with full details");

        List<Attribute> attributes = attributeRepository.findAllWithTranslations();

        // Для кожного атрибута довантажуємо опції та їх переклади
        for (Attribute attr : attributes) {
            if (attr.getId() != null) {
                // Довантажуємо опції
                attributeRepository.findByIdWithOptions(attr.getId());

                // Для кожної опції довантажуємо переклади
                if (attr.getOptions() != null && !attr.getOptions().isEmpty()) {
                    for (AttributeOption option : attr.getOptions()) {
                        if (option.getId() != null) {
                            attributeRepository.findOptionByIdWithTranslations(option.getId());
                        }
                    }
                }
            }
        }

        return attributes.stream()
                .map(this::convertToDtoWithOptions)
                .collect(Collectors.toList());
    }

    /**
     * Створити новий атрибут
     */
    @Transactional
    public AttributeDto createAttribute(AttributeDto dto) {
        log.info("Creating new attribute: name={}, type={}", dto.getName(), dto.getType());

        // Валідація
        validateAttributeDto(dto);

        // Перевірка унікальності імені
        if (attributeRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Attribute with this name already exists: " + dto.getName());
        }

        // Створення нової сутності
        Attribute attribute = new Attribute();
        mapDtoToEntity(dto, attribute);

        // Збереження
        Attribute saved = attributeRepository.save(attribute);
        log.info("Attribute created successfully with id: {}", saved.getId());

        return convertToDtoWithOptions(saved);
    }

    /**
     * Оновити існуючий атрибут
     */
    @Transactional
    public AttributeDto updateAttribute(Long id, AttributeDto dto) {
        log.info("Updating attribute: id={}, name={}", id, dto.getName());

        // Завантажуємо атрибут з усіма деталями
        Attribute attribute = findAttributeWithAllDetails(id);

        // Валідація
        validateAttributeDto(dto);

        // Перевірка унікальності імені (якщо змінюється)
        if (!attribute.getName().equals(dto.getName()) &&
                attributeRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Attribute with this name already exists: " + dto.getName());
        }

        // Оновлення даних
        mapDtoToEntity(dto, attribute);

        // Збереження
        Attribute updated = attributeRepository.save(attribute);
        log.info("Attribute updated successfully: id={}", updated.getId());

        return convertToDtoWithOptions(updated);
    }

    /**
     * Видалити атрибут
     */
    @Transactional
    public void deleteAttribute(Long id) {
        log.info("Deleting attribute: id={}", id);

        // Перевірка існування
        if (!attributeRepository.existsById(id)) {
            throw new IllegalArgumentException("Attribute not found with id: " + id);
        }

        // Можна додати перевірку на використання атрибута
        // Наприклад, чи не використовується він в продуктах

        attributeRepository.deleteById(id);
        log.info("Attribute deleted successfully: id={}", id);
    }

    // ========== МЕТОДИ ДЛЯ PLACEMENTS ==========

    /**
     * Отримати всі типи розміщень
     */
    public List<AttributePlacementDto> getAllPlacements() {
        log.debug("Getting all attribute placements");

        return placementRepository.findAll().stream()
                .map(this::convertPlacementToDto)
                .collect(Collectors.toList());
    }

    /**
     * Створити новий тип розміщення
     */
    @Transactional
    public AttributePlacementDto createPlacement(AttributePlacementDto dto) {
        log.info("Creating new placement: key={}", dto.getKey());

        // Валідація
        if (dto.getKey() == null || dto.getKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Placement key is required");
        }

        // Перевірка унікальності ключа
        if (placementRepository.existsByKey(dto.getKey())) {
            throw new IllegalArgumentException("Placement with this key already exists: " + dto.getKey());
        }

        // Створення нової сутності через builder (альтернатива конструктору)
        AttributePlacement placement = AttributePlacement.builder()
                .key(dto.getKey())
                .description(dto.getDescription())
                .build();

        // Збереження
        AttributePlacement saved = placementRepository.save(placement);
        log.info("Placement created successfully with id: {}", saved.getId());

        return convertPlacementToDto(saved);
    }

    /**
     * Видалити тип розміщення
     */
    @Transactional
    public void deletePlacement(Integer id) {
        log.info("Deleting placement: id={}", id);

        // Перевірка існування
        if (!placementRepository.existsById(id)) {
            throw new IllegalArgumentException("Placement not found with id: " + id);
        }

        // Можна додати перевірку на використання
        // Наприклад, чи не використовується цей placement в атрибутах

        placementRepository.deleteById(id);
        log.info("Placement deleted successfully: id={}", id);
    }

    // ========== ДОПОМІЖНІ МЕТОДИ - ВАЛІДАЦІЯ ==========

    /**
     * Валідація DTO атрибута
     */
    private void validateAttributeDto(AttributeDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute name is required");
        }

        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute type is required");
        }

        // Перевірка валідності типу
        List<String> validTypes = Arrays.asList("select", "multiselect", "text", "number");
        if (!validTypes.contains(dto.getType())) {
            throw new IllegalArgumentException("Invalid attribute type: " + dto.getType() +
                    ". Valid types are: " + String.join(", ", validTypes));
        }

        // Для select і multiselect перевіряємо наявність опцій
        if (("select".equals(dto.getType()) || "multiselect".equals(dto.getType())) &&
                (dto.getOptions() == null || dto.getOptions().isEmpty())) {
            throw new IllegalArgumentException("Attributes of type 'select' or 'multiselect' must have at least one option");
        }
    }

    // ========== ДОПОМІЖНІ МЕТОДИ - КОНВЕРТАЦІЯ ==========

    /**
     * Конвертація Entity -> DTO (без опцій)
     * Використовується для легкої версії списку
     */
    private AttributeDto convertToDto(Attribute attribute) {
        AttributeDto dto = new AttributeDto();
        dto.setId(attribute.getId());
        dto.setName(attribute.getName());
        dto.setType(attribute.getType());
        dto.setSortOrder(attribute.getSortOrder());

        // Мапінг перекладів
        if (attribute.getTranslations() != null) {
            Map<String, String> translations = new HashMap<>();
            for (AttributeTranslation translation : attribute.getTranslations()) {
                translations.put(translation.getLangCode(), translation.getLabel());
            }
            dto.setTranslations(translations);
        }

        return dto;
    }

    /**
     * Конвертація Entity -> DTO (з опціями та їх перекладами)
     * Використовується для детального перегляду
     */
    private AttributeDto convertToDtoWithOptions(Attribute attribute) {
        AttributeDto dto = convertToDto(attribute);

        // Мапінг опцій
        if (attribute.getOptions() != null && !attribute.getOptions().isEmpty()) {
            List<AttributeOptionDto> optionDtos = new ArrayList<>();

            for (AttributeOption option : attribute.getOptions()) {
                AttributeOptionDto optionDto = new AttributeOptionDto();
                optionDto.setId(option.getId());
                optionDto.setValue(option.getValue());
                optionDto.setSortOrder(option.getSortOrder());

                // Мапінг перекладів опцій
                if (option.getTranslations() != null && !option.getTranslations().isEmpty()) {
                    Map<String, String> optionTranslations = new HashMap<>();
                    for (OptionTranslation translation : option.getTranslations()) {
                        optionTranslations.put(translation.getLangCode(), translation.getLabel());
                    }
                    optionDto.setTranslations(optionTranslations);
                }

                optionDtos.add(optionDto);
            }

            // Сортуємо опції за sortOrder
            optionDtos.sort(Comparator.comparing(AttributeOptionDto::getSortOrder,
                    Comparator.nullsLast(Comparator.naturalOrder())));

            dto.setOptions(optionDtos);

            log.debug("Converted {} options for attribute {}", optionDtos.size(), attribute.getId());
        } else {
            log.debug("No options found for attribute {}", attribute.getId());
        }

        return dto;
    }

    /**
     * Мапінг DTO -> Entity
     * Використовується при створенні та оновленні
     */
    private void mapDtoToEntity(AttributeDto dto, Attribute attribute) {
        attribute.setName(dto.getName());
        attribute.setType(dto.getType());
        attribute.setSortOrder(dto.getSortOrder());

        // Мапінг перекладів
        updateTranslations(attribute, dto.getTranslations());

        // Мапінг опцій (якщо є)
        if (dto.getOptions() != null) {
            updateOptions(attribute, dto.getOptions());
        }
    }

    /**
     * Оновлення перекладів атрибута
     */
    private void updateTranslations(Attribute attribute, Map<String, String> translationsMap) {
        if (translationsMap == null || translationsMap.isEmpty()) {
            return;
        }

        // Видаляємо старі переклади, яких немає в новій мапі
        if (attribute.getTranslations() != null) {
            attribute.getTranslations().removeIf(translation ->
                    !translationsMap.containsKey(translation.getLangCode()) ||
                            translationsMap.get(translation.getLangCode()) == null ||
                            translationsMap.get(translation.getLangCode()).trim().isEmpty()
            );
        } else {
            attribute.setTranslations(new ArrayList<>());
        }

        // Додаємо або оновлюємо переклади
        for (Map.Entry<String, String> entry : translationsMap.entrySet()) {
            String language = entry.getKey();
            String label = entry.getValue();

            if (label == null || label.trim().isEmpty()) {
                continue;
            }

            // Шукаємо існуючий переклад
            Optional<AttributeTranslation> existingTranslation = attribute.getTranslations().stream()
                    .filter(t -> t.getLangCode().equals(language))
                    .findFirst();

            if (existingTranslation.isPresent()) {
                // Оновлюємо існуючий
                existingTranslation.get().setLabel(label);
            } else {
                // Створюємо новий
                AttributeTranslation translation = new AttributeTranslation();
                translation.setAttribute(attribute);
                translation.setLangCode(language);
                translation.setLabel(label);
                attribute.getTranslations().add(translation);
            }
        }
    }

    /**
     * Оновлення опцій атрибута
     */
    private void updateOptions(Attribute attribute, List<AttributeOptionDto> optionDtos) {
        if (attribute.getOptions() == null) {
            attribute.setOptions(new ArrayList<>());
        }

        // Збираємо ID опцій з DTO
        Set<Long> dtoOptionIds = optionDtos.stream()
                .map(AttributeOptionDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Видаляємо опції, яких немає в DTO
        attribute.getOptions().removeIf(option ->
                option.getId() != null && !dtoOptionIds.contains(option.getId())
        );

        // Додаємо або оновлюємо опції
        for (AttributeOptionDto optionDto : optionDtos) {
            if (optionDto.getId() != null) {
                // Оновлюємо існуючу опцію
                Optional<AttributeOption> existingOption = attribute.getOptions().stream()
                        .filter(opt -> optionDto.getId().equals(opt.getId()))
                        .findFirst();

                if (existingOption.isPresent()) {
                    updateOption(existingOption.get(), optionDto);
                }
            } else {
                // Створюємо нову опцію
                AttributeOption newOption = new AttributeOption();
                newOption.setAttribute(attribute);
                updateOption(newOption, optionDto);
                attribute.getOptions().add(newOption);
            }
        }
    }

    /**
     * Оновлення однієї опції
     */
    private void updateOption(AttributeOption option, AttributeOptionDto dto) {
        option.setValue(dto.getValue());
        option.setSortOrder(dto.getSortOrder());

        // Оновлюємо переклади опції
        if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
            if (option.getTranslations() == null) {
                option.setTranslations(new ArrayList<>());
            }

            // Видаляємо старі переклади, яких немає в новій мапі
            option.getTranslations().removeIf(translation ->
                    !dto.getTranslations().containsKey(translation.getLangCode()) ||
                            dto.getTranslations().get(translation.getLangCode()) == null ||
                            dto.getTranslations().get(translation.getLangCode()).trim().isEmpty()
            );

            // Додаємо або оновлюємо переклади
            for (Map.Entry<String, String> entry : dto.getTranslations().entrySet()) {
                String language = entry.getKey();
                String label = entry.getValue();

                if (label == null || label.trim().isEmpty()) {
                    continue;
                }

                // Шукаємо існуючий переклад
                Optional<OptionTranslation> existingTranslation = option.getTranslations().stream()
                        .filter(t -> t.getLangCode().equals(language))
                        .findFirst();

                if (existingTranslation.isPresent()) {
                    // Оновлюємо існуючий
                    existingTranslation.get().setLabel(label);
                } else {
                    // Створюємо новий
                    OptionTranslation translation = new OptionTranslation();
                    translation.setOption(option);
                    translation.setLangCode(language);
                    translation.setLabel(label);
                    option.getTranslations().add(translation);
                }
            }
        }
    }

    /**
     * Конвертація Placement Entity -> DTO
     */
    private AttributePlacementDto convertPlacementToDto(AttributePlacement placement) {
        AttributePlacementDto dto = new AttributePlacementDto();
        dto.setId(placement.getId());
        dto.setKey(placement.getKey());
        dto.setDescription(placement.getDescription());
        return dto;
    }
}
