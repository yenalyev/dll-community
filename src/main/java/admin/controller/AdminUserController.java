package admin.controller;

import admin.dto.AdminUserDTO;
import admin.dto.UserStatisticsDTO;
import admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService userService;

    /**
     * Список всіх користувачів
     * URL: /admin/users
     */
    @GetMapping({"", "/"})
    public String listUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean hasSubscription,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        log.info("Users page accessed: email={}, role={}, page={}", email, role, page);

        // Отримати користувачів з фільтрами
        Page<AdminUserDTO> usersPage = userService.getUsersWithFilters(
                email, name, role, isActive, hasSubscription, from, to, page, size
        );

        // Отримати статистику
        UserStatisticsDTO statistics = userService.getUserStatistics();

        model.addAttribute("pageTitle", "Manage Users");
        model.addAttribute("users", usersPage);
        model.addAttribute("statistics", statistics);

        // Фільтри для форми
        model.addAttribute("roles", new String[]{"USER", "MANAGER", "ADMIN"});
        model.addAttribute("selectedEmail", email);
        model.addAttribute("selectedName", name);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedIsActive", isActive);
        model.addAttribute("selectedHasSubscription", hasSubscription);
        model.addAttribute("selectedFrom", from);
        model.addAttribute("selectedTo", to);

        return "admin/users/list";
    }

    /**
     * Деталі користувача
     * URL: /admin/users/{id}
     */
    @GetMapping("/{id}")
    public String userDetails(@PathVariable Long id, Model model) {
        log.info("User details page accessed: id={}", id);

        try {
            AdminUserDTO user = userService.getUserById(id);

            model.addAttribute("pageTitle", "User: " + user.getName());
            model.addAttribute("user", user);
            model.addAttribute("availableRoles", new String[]{"USER", "MANAGER", "ADMIN"});

            return "admin/users/details";

        } catch (IllegalArgumentException e) {
            log.error("User not found: {}", id);
            model.addAttribute("errorMessage", "Користувача не знайдено");
            return "redirect:/admin/users";
        }
    }

    /**
     * Заблокувати/розблокувати користувача
     * URL: /admin/users/{id}/toggle-active
     */
    @PostMapping("/{id}/toggle-active")
    public String toggleUserActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Toggling user active status: id={}", id);

        try {
            userService.toggleUserActiveStatus(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Статус користувача успішно змінено!");
        } catch (Exception e) {
            log.error("Error toggling user status", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Помилка зміни статусу: " + e.getMessage());
        }

        return "redirect:/admin/users/" + id;
    }

    /**
     * Змінити роль користувача
     * URL: /admin/users/{id}/change-role
     */
    @PostMapping("/{id}/change-role")
    public String changeUserRole(
            @PathVariable Long id,
            @RequestParam String newRole,
            RedirectAttributes redirectAttributes
    ) {
        log.info("Changing user role: id={}, newRole={}", id, newRole);

        try {
            userService.changeUserRole(id, newRole);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Роль користувача успішно змінено!");
        } catch (Exception e) {
            log.error("Error changing user role", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Помилка зміни ролі: " + e.getMessage());
        }

        return "redirect:/admin/users/" + id;
    }

    /**
     * Подарувати підписку
     * URL: /admin/users/{id}/gift-subscription
     */
    @PostMapping("/{id}/gift-subscription")
    public String giftSubscription(
            @PathVariable Long id,
            @RequestParam Long planId,
            RedirectAttributes redirectAttributes
    ) {
        log.info("Gifting subscription: userId={}, planId={}", id, planId);

        try {
            userService.giftSubscription(id, planId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Підписку успішно подаровано!");
        } catch (Exception e) {
            log.error("Error gifting subscription", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Помилка: " + e.getMessage());
        }

        return "redirect:/admin/users/" + id;
    }

    // TODO: Додати методи для:
    // - Скидання пароля
    // - Відправки email
    // - Експорт в CSV/Excel
}