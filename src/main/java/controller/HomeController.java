package controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контролер для обробки запитів, пов'язаних з головною (стартовою) сторінкою сайту.
 */
@Controller
public class HomeController {

    /**
     * Обробляє GET-запити на кореневий URL ("/"), а також на "/home" та "/index".
     *
     * @param model об'єкт Model для передачі даних у шаблон.
     * @return назва Thymeleaf-шаблону для відображення ("home").
     */
    @GetMapping({"/", "/home", "/index"})
    public String homePage(Model model) {
        // На цьому етапі ми просто повертаємо сторінку.
        // У майбутньому тут можна додати логіку для передачі даних на головну сторінку.
        // Наприклад, список популярних уроків:
        // model.addAttribute("popularLessons", lessonService.findPopular());
        model.addAttribute("welcomeMessage", "Ласкаво просимо на нашу освітню платформу!");
        return "home"; // Ця назва відповідає файлу home.html у папці templates
    }
}
