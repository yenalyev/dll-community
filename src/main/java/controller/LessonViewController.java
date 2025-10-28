package controller;

import admin.dto.LessonDto;
import admin.dto.LessonTranslationDto;
import admin.service.LessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контролер для відображення окремого уроку на фронтенді
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/{lang}/lessons")
public class LessonViewController {

    private final LessonService lessonService;

    /**
     * Відображення уроку за slug
     * URL: /uk/lessons/autumn-debate-cafe
     *
     * @param lang код мови (uk, en, de)
     * @param slug slug уроку для певної мови
     * @param model модель для передачі даних у view
     * @return назва темплейту
     */
    @GetMapping("/{slug}")
    public String viewLesson(
            @PathVariable String lang,
            @PathVariable String slug,
            Model model) {

        log.info("Viewing lesson: lang={}, slug={}", lang, slug);

        try {
            // Отримуємо повні дані уроку
            LessonDto lesson = lessonService.getLessonBySlugAndLang(slug, lang);

            // Отримуємо переклад для поточної мови
            LessonTranslationDto translation = lesson.getTranslations().get(lang);

            if (translation == null) {
                log.warn("No translation found for lesson slug={}, lang={}", slug, lang);
                return "redirect:/{lang}/lessons";
            }

            // Передаємо дані у view
            model.addAttribute("lesson", lesson);
            model.addAttribute("translation", translation);
            model.addAttribute("pageTitle", translation.getTitle());

            log.debug("Lesson loaded: id={}, title={}", lesson.getId(), translation.getTitle());

            return "lessons/lesson";

        } catch (IllegalArgumentException e) {
            log.error("Lesson not found: slug={}, lang={}", slug, lang);
            return "redirect:/{lang}/lessons";
        }
    }

    /**
     * АЛЬТЕРНАТИВА: Відображення уроку за ID
     * URL: /uk/lessons/view/123
     *
     * Можна використовувати як запасний варіант або для адмін-перегляду
     */
    @GetMapping("/view/{id}")
    public String viewLessonById(
            @PathVariable String lang,
            @PathVariable Long id,
            Model model) {

        log.info("Viewing lesson by ID: lang={}, id={}", lang, id);

        try {
            LessonDto lesson = lessonService.getLessonById(id);
            LessonTranslationDto translation = lesson.getTranslations().get(lang);

            if (translation == null) {
                log.warn("No translation found for lesson id={}, lang={}", id, lang);
                return "redirect:/{lang}/lessons";
            }

            model.addAttribute("lesson", lesson);
            model.addAttribute("translation", translation);
            model.addAttribute("pageTitle", translation.getTitle());

            return "lessons/lesson";

        } catch (IllegalArgumentException e) {
            log.error("Lesson not found: id={}", id);
            return "redirect:/{lang}/lessons";
        }
    }
}
