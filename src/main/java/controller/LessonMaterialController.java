package controller;

import entity.lesson.Lesson;
import entity.lesson.LessonMaterial;
import entity.lesson.LessonTranslation;
import entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import platform.config.security.CustomUserDetails;
import repository.LessonRepository;
import service.subscription.LessonAccessService;
import service.user.UserService;

/**
 * Контролер для доступу до матеріалів уроку
 * Перевіряє чи має користувач доступ до матеріалів платних уроків
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/{lang}/lessons/{lessonId}/materials")
public class LessonMaterialController {

    private final LessonRepository lessonRepository;
    private final LessonAccessService lessonAccessService;
    private final UserService userService;

    /**
     * Доступ до матеріалу уроку
     *
     * URL: /uk/lessons/123/materials/456
     *
     * Логіка:
     * 1. Завантажити урок і матеріал
     * 2. Перевірити доступ через LessonAccessService
     * 3. Якщо немає доступу - redirect з параметрами для модалки
     * 4. Якщо є доступ - redirect до файлу (відкривається в новому табі)
     */
    @GetMapping("/{materialId}")
    public String accessMaterial(
            @PathVariable String lang,
            @PathVariable Long lessonId,
            @PathVariable Long materialId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        log.info("Material access attempt: lessonId={}, materialId={}, user={}",
                lessonId, materialId,
                userDetails != null ? userDetails.getId() : "anonymous");

        try {
            // 1. Завантажити урок з матеріалами та перекладами
            Lesson lesson = lessonRepository.findByIdWithMaterials(lessonId)
                    .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + lessonId));

            // Завантажити переклади окремо (уникаємо MultipleBagFetchException)
            Lesson lessonWithTranslations = lessonRepository.findByIdWithTranslations(lessonId)
                    .orElse(lesson);
            lesson.setTranslations(lessonWithTranslations.getTranslations());

            // 2. Знайти потрібний матеріал
            LessonMaterial material = lesson.getMaterials().stream()
                    .filter(m -> m.getId().equals(materialId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Material not found: " + materialId));

            // 3. Отримати User object (може бути null якщо не залогінений)
            User user = null;
            if (userDetails != null) {
                user = userService.findById(userDetails.getId());
            }

            // 4. Перевірити доступ
            boolean hasAccess = lessonAccessService.hasAccessToLesson(user, lesson);

            if (!hasAccess) {
                // Немає доступу - визначаємо причину
                LessonAccessService.AccessDenialReason reason =
                        lessonAccessService.getAccessDenialReason(user, lesson);

                log.info("Access denied: lessonId={}, reason={}", lessonId, reason);

                // Редірект назад на сторінку уроку з параметрами для модалки
                redirectAttributes.addAttribute("accessDenied", true);
                redirectAttributes.addAttribute("reason", reason.name());
                redirectAttributes.addAttribute("materialId", materialId);

                // Отримуємо slug для редіректу
                String slug = lesson.getTranslations().stream()
                        .filter(t -> t.getLang().equals(lang))
                        .findFirst()
                        .map(LessonTranslation::getSlug)
                        .orElse(lessonId.toString());

                return "redirect:/{lang}/lessons/" + slug;
            }

            // 5. Є доступ - redirect до файлу
            log.info("Access granted: lessonId={}, materialId={}, user={}",
                    lessonId, materialId, user != null ? user.getId() : "free");

            // Матеріали - це лінки на сторонні ресурси, просто редірект
            return "redirect:" + material.getContent();

        } catch (IllegalArgumentException e) {
            log.error("Error accessing material: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Матеріал не знайдено");
            return "redirect:/{lang}/lessons";
        } catch (Exception e) {
            log.error("Unexpected error accessing material", e);
            redirectAttributes.addFlashAttribute("error",
                    "Виникла помилка при доступі до матеріалу");
            return "redirect:/{lang}/lessons";
        }
    }
}
