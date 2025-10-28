package controller;

import dto.LessonCardDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import service.LessonCardService;

import java.util.List;

/**
 * ПРИКЛАД контролера для сторінки зі списком уроків
 *
 * Цей контролер демонструє, як використовувати LessonCardService
 * для підготовки даних і передачі їх у view.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/{lang}/lessons")
public class LessonCatalogController {

    private final LessonCardService lessonCardService;

    /**
     * Сторінка зі списком усіх уроків
     * URL: /uk/lessons, /en/lessons, /de/lessons
     */
    @GetMapping
    public String lessonCatalog(@PathVariable String lang, Model model) {
        log.info("Lesson catalog page accessed for language: {}", lang);

        // Отримуємо картки всіх уроків для поточної мови
        List<LessonCardDto> lessonCards = lessonCardService.getAllLessonCards(lang);

        // Передаємо дані у view
        model.addAttribute("lessonCards", lessonCards);
        model.addAttribute("pageTitle", "All Lessons");

        return "lessons/catalog";
    }
}
