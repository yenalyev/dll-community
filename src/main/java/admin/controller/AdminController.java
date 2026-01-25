package admin.controller;

import admin.dto.DashboardStatsDTO;
import admin.dto.RecentOrderDTO;
import admin.dto.RecentUserDTO;
import admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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

    private final AdminDashboardService dashboardService;

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        // Отримати статистику
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);

        // Отримати останні замовлення (3 шт)
        List<RecentOrderDTO> recentOrders = dashboardService.getRecentOrders(3);
        model.addAttribute("recentOrders", recentOrders);

        // Отримати останніх користувачів (4 шт)
        List<RecentUserDTO> recentUsers = dashboardService.getRecentUsers(4);
        model.addAttribute("recentUsers", recentUsers);

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



