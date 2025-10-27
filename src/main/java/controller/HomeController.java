package controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контролер для обробки запитів, пов'язаних з головною (стартовою) сторінкою сайту.
 */
@Controller
@RequestMapping("/{lang}") // Префікс для всіх URL в цьому класі
public class HomeController {

    /**
     * Обробляє GET-запити на кореневий URL ("/{lang}/"), а також на "/{lang}/home" та "/{lang}/index".
     *
     * @param lang  змінна шляху, що містить код мови (напр., "uk", "en").
     * @param model об'єкт Model для передачі даних у шаблон.
     * @return назва Thymeleaf-шаблону для відображення ("home").
     */
    @GetMapping({"/", "/home", "/index"})
    public String homePage(@PathVariable String lang, Model model) { // <--- КЛЮЧОВА ЗМІНА
        // Тепер метод знає про мову з URL.
        // Хоча ми можемо не використовувати змінну 'lang' прямо тут
        // (оскільки локаль вже встановлена перехоплювачем),
        // її наявність у сигнатурі методу є обов'язковою для Spring.
        model.addAttribute("welcomeMessage", "Ласкаво просимо на нашу освітню платформу!");
        return "home";
    }
}
