package admin.controller;

import admin.dto.LessonDto;
import admin.service.AttributeService;
import admin.service.LessonService;
import entity.lesson.Lesson;
import entity.lesson.LessonMaterial;
import entity.lesson.LessonPrice;
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
@RequestMapping("/admin/lessons")
@PreAuthorize("hasRole('ADMIN')")
public class LessonController {

    private final LessonService lessonService;
    private final AttributeService attributeService;

    // Доступні мови
    private static final List<String> AVAILABLE_LANGUAGES = Arrays.asList("uk", "en", "de");

    /**
     * Головна сторінка управління уроками
     */
    @GetMapping({"", "/"})
    public String lessonsPage(Model model) {
        log.info("Lessons page accessed");

        model.addAttribute("pageTitle", "Manage Lessons");
        model.addAttribute("lessons", lessonService.getAllLessons());

        return "admin/lessons/index";
    }

    /**
     * Сторінка створення нового уроку
     */
    @GetMapping("/create")
    public String createLessonPage(Model model) {
        log.info("Create lesson page accessed");

        LessonDto dto = new LessonDto();
        dto.initializeTranslations(); // Ініціалізуємо переклади для всіх мов

        model.addAttribute("pageTitle", "Create Lesson");
        model.addAttribute("lesson", dto);
        model.addAttribute("availableLanguages", AVAILABLE_LANGUAGES);
        model.addAttribute("accessLevels", Lesson.AccessLevel.values());
        model.addAttribute("currencies", LessonPrice.Currency.values());
        model.addAttribute("materialTypes", LessonMaterial.MaterialType.values());
        model.addAttribute("attributes", attributeService.getAllAttributes());
        model.addAttribute("isEdit", false);

        return "admin/lessons/form";
    }

    /**
     * Збереження нового уроку
     */
    @PostMapping("/create")
    public String createLesson(
            @Valid @ModelAttribute("lesson") LessonDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Creating lesson: {}", dto.getTranslations().get("uk") != null ?
                dto.getTranslations().get("uk").getTitle() : "New Lesson");

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("pageTitle", "Create Lesson");
            model.addAttribute("availableLanguages", AVAILABLE_LANGUAGES);
            model.addAttribute("accessLevels", Lesson.AccessLevel.values());
            model.addAttribute("currencies", LessonPrice.Currency.values());
            model.addAttribute("materialTypes", LessonMaterial.MaterialType.values());
            model.addAttribute("attributes", attributeService.getAllAttributes());
            model.addAttribute("isEdit", false);
            return "admin/lessons/form";
        }

        try {
            LessonDto created = lessonService.createLesson(dto);
            log.info("Lesson created successfully: id={}", created.getId());

            redirectAttributes.addFlashAttribute("success", "Lesson created successfully!");
            return "redirect:/admin/lessons";

        } catch (IllegalArgumentException e) {
            log.error("Error creating lesson: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pageTitle", "Create Lesson");
            model.addAttribute("availableLanguages", AVAILABLE_LANGUAGES);
            model.addAttribute("accessLevels", Lesson.AccessLevel.values());
            model.addAttribute("currencies", LessonPrice.Currency.values());
            model.addAttribute("materialTypes", LessonMaterial.MaterialType.values());
            model.addAttribute("attributes", attributeService.getAllAttributes());
            model.addAttribute("isEdit", false);
            return "admin/lessons/form";
        }
    }

    /**
     * Сторінка редагування уроку
     */
    @GetMapping("/edit/{id}")
    public String editLessonPage(@PathVariable Long id, Model model) {
        log.info("Edit lesson page accessed: id={}", id);

        LessonDto dto = lessonService.getLessonById(id);

        // Додаємо порожні переклади для мов, яких немає
        dto.initializeTranslations();

        model.addAttribute("pageTitle", "Edit Lesson");
        model.addAttribute("lesson", dto);
        model.addAttribute("availableLanguages", AVAILABLE_LANGUAGES);
        model.addAttribute("accessLevels", Lesson.AccessLevel.values());
        model.addAttribute("currencies", LessonPrice.Currency.values());
        model.addAttribute("materialTypes", LessonMaterial.MaterialType.values());
        model.addAttribute("attributes", attributeService.getAllAttributes());
        model.addAttribute("isEdit", true);

        return "admin/lessons/form";
    }

    /**
     * Оновлення уроку
     */
    @PostMapping("/edit/{id}")
    public String updateLesson(
            @PathVariable Long id,
            @Valid @ModelAttribute("lesson") LessonDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Updating lesson: id={}", id);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("pageTitle", "Edit Lesson");
            model.addAttribute("availableLanguages", AVAILABLE_LANGUAGES);
            model.addAttribute("accessLevels", Lesson.AccessLevel.values());
            model.addAttribute("currencies", LessonPrice.Currency.values());
            model.addAttribute("materialTypes", LessonMaterial.MaterialType.values());
            model.addAttribute("attributes", attributeService.getAllAttributes());
            model.addAttribute("isEdit", true);
            return "admin/lessons/form";
        }

        try {
            LessonDto updated = lessonService.updateLesson(id, dto);
            log.info("Lesson updated successfully: id={}", updated.getId());

            redirectAttributes.addFlashAttribute("success", "Lesson updated successfully!");
            return "redirect:/admin/lessons";

        } catch (IllegalArgumentException e) {
            log.error("Error updating lesson: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pageTitle", "Edit Lesson");
            model.addAttribute("availableLanguages", AVAILABLE_LANGUAGES);
            model.addAttribute("accessLevels", Lesson.AccessLevel.values());
            model.addAttribute("currencies", LessonPrice.Currency.values());
            model.addAttribute("materialTypes", LessonMaterial.MaterialType.values());
            model.addAttribute("attributes", attributeService.getAllAttributes());
            model.addAttribute("isEdit", true);
            return "admin/lessons/form";
        }
    }

    /**
     * Видалення уроку
     */
    @PostMapping("/delete/{id}")
    public String deleteLesson(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        log.info("Deleting lesson: id={}", id);

        try {
            lessonService.deleteLesson(id);
            log.info("Lesson deleted successfully: id={}", id);

            redirectAttributes.addFlashAttribute("success", "Lesson deleted successfully!");

        } catch (Exception e) {
            log.error("Error deleting lesson: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error deleting lesson: " + e.getMessage());
        }

        return "redirect:/admin/lessons";
    }

    /**
     * API endpoint для отримання уроку у форматі JSON
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<LessonDto> getLessonJson(@PathVariable Long id) {
        try {
            LessonDto dto = lessonService.getLessonById(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
