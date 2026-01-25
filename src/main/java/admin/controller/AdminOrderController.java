package admin.controller;

import admin.dto.AdminOrderDTO;
import admin.dto.OrderStatisticsDTO;
import admin.service.AdminOrderService;
import entity.enums.OrderStatus;
import entity.enums.OrderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService orderService;

    /**
     * Список всіх замовлень
     * URL: /admin/orders
     */
    @GetMapping({"", "/"})
    public String listOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) OrderType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        log.info("Orders page accessed: status={}, type={}, page={}", status, type, page);

        // Отримати замовлення з фільтрами
        Page<AdminOrderDTO> ordersPage = orderService.getOrdersWithFilters(
                status, type, from, to, email, page, size
        );

        // Отримати статистику
        OrderStatisticsDTO statistics = orderService.getOrderStatistics();

        model.addAttribute("pageTitle", "Manage Orders");
        model.addAttribute("orders", ordersPage);
        model.addAttribute("statistics", statistics);

        // Фільтри для форми
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("orderTypes", OrderType.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedEmail", email);
        model.addAttribute("selectedFrom", from);
        model.addAttribute("selectedTo", to);

        return "admin/orders/list";
    }

    /**
     * Деталі замовлення
     * URL: /admin/orders/{id}
     */
    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id, Model model) {
        log.info("Order details page accessed: id={}", id);

        try {
            AdminOrderDTO order = orderService.getOrderById(id);

            model.addAttribute("pageTitle", "Order #" + id);
            model.addAttribute("order", order);

            return "admin/orders/details";

        } catch (IllegalArgumentException e) {
            log.error("Order not found: {}", id);
            model.addAttribute("errorMessage", "Замовлення не знайдено");
            return "redirect:/admin/orders";
        }
    }

    // TODO: Додати методи для:
    // - Скасування замовлення
    // - Повернення коштів (refund)
    // - Експорт в CSV/Excel
}