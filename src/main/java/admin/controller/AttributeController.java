package admin.controller;

import admin.dto.AttributeDto;
import admin.dto.AttributePlacementDto;
import admin.service.AttributeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/attributes")
@PreAuthorize("hasRole('ADMIN')")
public class AttributeController {

    private final AttributeService attributeService;

    // Доступні мови для перекладів
    private static final List<String> AVAILABLE_LANGUAGES = Arrays.asList("uk", "en", "de");

    // Доступні типи атрибутів
    private static final List<String> ATTRIBUTE_TYPES = Arrays.asList("select", "multiselect", "text", "number");

    /**
     * Головна сторінка управління атрибутами
     */
    @GetMapping({"", "/"})
    public String attributesPage(Model model) {
        log.info("Attributes page accessed");

        model.addAttribute("pageTitle", "Manage Attributes");
        model.addAttribute("attributes", attributeService.getAllAttributesWithDetails());
        model.addAttribute("placements", attributeService.getAllPlacements());

        System.out.println("attributes - " + attributeService.getAllAttributesWithDetails());

        return "admin/attributes/index";
    }

    /**
     * Сторінка створення нового атрибута
     */
    @GetMapping("/create")
    public String createAttributePage(Model model) {
        log.info("Create attribute page accessed");

        AttributeDto dto = new AttributeDto();
        // Ініціалізуємо порожні переклади для всіх мов
        for (String lang : AVAILABLE_LANGUAGES) {
            dto.getTranslations().put(lang, "");
        }

        model.addAttribute("pageTitle", "Create Attribute");
        model.addAttribute("attribute", dto);
        model.addAttribute("availableLanguages", AVAILABLE_LANGUAGES);
        model.addAttribute("attributeTypes", ATTRIBUTE_TYPES);
        model.addAttribute("placements", attributeService.getAllPlacements());
        model.addAttribute("isEdit", false);

        return "admin/attributes/form";
    }

    /**
     * Збереження нового атрибута
     */
    @PostMapping("/create")
    public String createAttribute(
            @Valid @ModelAttribute("attribute") AttributeDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Creating attribute: {}", dto.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("pageTitle", "Create Attribute");
            model.addAttribute("availableLanguages", AVAILABLE_LANGUAGES);
            model.addAttribute("attributeTypes", ATTRIBUTE_TYPES);
            model.addAttribute("placements", attributeService.getAllPlacements());
            model.addAttribute("isEdit", false);
            return "admin/attributes/form";
        }

        try {
            AttributeDto created = attributeService.createAttribute(dto);
            log.info("Attribute created successfully: id={}", created.getId());

            redirectAttributes.addFlashAttribute("success", "Attribute created successfully!");
            return "redirect:/admin/attributes";

        } catch (IllegalArgumentException e) {
            log.error("Error creating attribute: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pageTitle", "Create Attribute");
            model.addAttribute("availableLanguages", AVAILABLE_LANGUAGES);
            model.addAttribute("attributeTypes", ATTRIBUTE_TYPES);
            model.addAttribute("placements", attributeService.getAllPlacements());
            model.addAttribute("isEdit", false);
            return "admin/attributes/form";
        }
    }

    /**
     * Сторінка редагування атрибута
     */
    @GetMapping("/edit/{id}")
    public String editAttributePage(@PathVariable Long id, Model model) {
        log.info("Edit attribute page accessed: id={}", id);

        AttributeDto dto = attributeService.getAttributeById(id);

        // ДЕТАЛЬНЕ ЛОГУВАННЯ
        log.info("=== ATTRIBUTE DATA ===");
        log.info("ID: {}", dto.getId());
        log.info("Name: {}", dto.getName());
        log.info("Type: {}", dto.getType());
        log.info("Options count: {}", dto.getOptions() != null ? dto.getOptions().size() : 0);

        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            log.info("=== OPTIONS DETAILS ===");
            dto.getOptions().forEach(opt -> {
                log.info("Option ID: {}, Value: {}, Translations: {}",
                        opt.getId(), opt.getValue(), opt.getTranslations());
            });
        } else {
            log.warn("NO OPTIONS FOUND FOR ATTRIBUTE {}", id);
        }
        log.info("======================");

        // Додаємо порожні переклади для мов, яких немає
        for (String lang : AVAILABLE_LANGUAGES) {
            dto.getTranslations().putIfAbsent(lang, "");
        }

        model.addAttribute("pageTitle", "Edit Attribute");
        model.addAttribute("attribute", dto);
        model.addAttribute("availableLanguages", AVAILABLE_LANGUAGES);
        model.addAttribute("attributeTypes", ATTRIBUTE_TYPES);
        model.addAttribute("placements", attributeService.getAllPlacements());
        model.addAttribute("isEdit", true);

        return "admin/attributes/form";
    }

    /**
     * Оновлення атрибута
     */
    @PostMapping("/edit/{id}")
    public String updateAttribute(
            @PathVariable Long id,
            @Valid @ModelAttribute("attribute") AttributeDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Updating attribute: id={}", id);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("pageTitle", "Edit Attribute");
            model.addAttribute("availableLanguages", AVAILABLE_LANGUAGES);
            model.addAttribute("attributeTypes", ATTRIBUTE_TYPES);
            model.addAttribute("placements", attributeService.getAllPlacements());
            model.addAttribute("isEdit", true);
            return "admin/attributes/form";
        }

        try {
            AttributeDto updated = attributeService.updateAttribute(id, dto);
            log.info("Attribute updated successfully: id={}, placements={}",
                    updated.getId(), updated.getPlacementIds());

            redirectAttributes.addFlashAttribute("success", "Attribute updated successfully!");
            return "redirect:/admin/attributes";

        } catch (IllegalArgumentException e) {
            log.error("Error updating attribute: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pageTitle", "Edit Attribute");
            model.addAttribute("availableLanguages", AVAILABLE_LANGUAGES);
            model.addAttribute("attributeTypes", ATTRIBUTE_TYPES);
            model.addAttribute("placements", attributeService.getAllPlacements());
            model.addAttribute("isEdit", true);
            return "admin/attributes/form";
        }
    }

    /**
     * Видалення атрибута
     */
    @PostMapping("/delete/{id}")
    public String deleteAttribute(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        log.info("Deleting attribute: id={}", id);

        try {
            attributeService.deleteAttribute(id);
            log.info("Attribute deleted successfully: id={}", id);

            redirectAttributes.addFlashAttribute("success", "Attribute deleted successfully!");

        } catch (Exception e) {
            log.error("Error deleting attribute: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error deleting attribute: " + e.getMessage());
        }

        return "redirect:/admin/attributes";
    }

    // ========== PLACEMENTS MANAGEMENT ==========

    /**
     * Сторінка управління типами розміщень
     */
    @GetMapping("/placements")
    public String placementsPage(Model model) {
        log.info("Placements page accessed");

        model.addAttribute("pageTitle", "Attribute Placements");
        model.addAttribute("placements", attributeService.getAllPlacements());
        model.addAttribute("newPlacement", new AttributePlacementDto());

        return "admin/attributes/placements";
    }

    /**
     * Створення нового типу розміщення
     */
    @PostMapping("/placements/create")
    public String createPlacement(
            @Valid @ModelAttribute("newPlacement") AttributePlacementDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Creating placement: {}", dto.getKey());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("pageTitle", "Attribute Placements");
            model.addAttribute("placements", attributeService.getAllPlacements());
            return "admin/attributes/placements";
        }

        try {
            attributeService.createPlacement(dto);
            log.info("Placement created successfully");

            redirectAttributes.addFlashAttribute("success", "Placement created successfully!");

        } catch (IllegalArgumentException e) {
            log.error("Error creating placement: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/attributes/placements";
    }

    /**
     * Видалення типу розміщення
     */
    @PostMapping("/placements/delete/{id}")
    public String deletePlacement(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        log.info("Deleting placement: id={}", id);

        try {
            attributeService.deletePlacement(id);
            log.info("Placement deleted successfully: id={}", id);

            redirectAttributes.addFlashAttribute("success", "Placement deleted successfully!");

        } catch (Exception e) {
            log.error("Error deleting placement: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error deleting placement: " + e.getMessage());
        }

        return "redirect:/admin/attributes/placements";
    }

    // ========== API ENDPOINTS (для AJAX) ==========

    /**
     * Отримати атрибут у форматі JSON
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<AttributeDto> getAttributeJson(@PathVariable Long id) {
        try {
            AttributeDto dto = attributeService.getAttributeById(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
