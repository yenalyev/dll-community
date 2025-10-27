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
     * Управління користувачами
     * URL: /admin/users
     */
    @GetMapping("/users")
    public String manageUsers(Model model) {
        log.info("Admin users page accessed");

        model.addAttribute("pageTitle", "Manage Users");

        return "admin/users";
    }

    /**
     * Управління уроками
     * URL: /admin/lessons
     */
    @GetMapping("/lessons")
    public String manageLessons(Model model) {
        log.info("Admin lessons page accessed");

        model.addAttribute("pageTitle", "Manage Lessons");

        return "admin/lessons";
    }

    /**
     * Управління замовленнями
     * URL: /admin/orders
     */
    @GetMapping("/orders")
    public String manageOrders(Model model) {
        log.info("Admin orders page accessed");

        model.addAttribute("pageTitle", "Manage Orders");

        return "admin/orders";
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



