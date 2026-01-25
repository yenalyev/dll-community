package admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контролер для адміністративної панелі.
 * Доступ лише для користувачів з роллю ADMIN.
 *
 * Адмінка одномовна і не використовує мовний префікс в URL.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    /**
     * Головна сторінка адмін-панелі
     * URL: /admin
     */
    @GetMapping({"", "/"})
    public String adminDashboard(Model model) {
        log.info("Admin dashboard accessed");

        model.addAttribute("pageTitle", "Admin Dashboard");

        return "admin/dashboard";
    }


    /**
     * Налаштування
     * URL: /admin/settings
     */
    @GetMapping("/settings")
    public String adminSettings(Model model) {
        log.info("Admin settings page accessed");

        model.addAttribute("pageTitle", "Settings");

        return "admin/settings";
    }
}



