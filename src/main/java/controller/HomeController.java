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
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/{lang}")
public class HomeController {

    private final LessonCardService lessonCardService;

    /**
     * Обробляє GET-запити на кореневий URL ("/{lang}/"), а також на "/{lang}/home" та "/{lang}/index".
     *
     * @param lang  змінна шляху, що містить код мови (напр., "uk", "en").
     * @param model об'єкт Model для передачі даних у шаблон.
     * @return назва Thymeleaf-шаблону для відображення ("home").
     */
    @GetMapping({"/", "/home", "/index"})
    public String homePage(@PathVariable String lang, Model model) {
        log.info("Home page accessed for language: {}", lang);

        // Отримуємо останні 9 уроків для поточної мови
        List<LessonCardDto> latestLessons = lessonCardService.getLatestLessonCards(lang, 9);

        log.debug("Loaded {} latest lessons for home page", latestLessons.size());

        // Передаємо дані у view
        model.addAttribute("welcomeMessage", "Ласкаво просимо на нашу освітню платформу!");
        model.addAttribute("latestLessons", latestLessons);

        return "home";
    }
}
