package controller;

import entity.order.Order;
import entity.order.SubscriptionPlan;
import entity.order.UserSubscription;
import entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import platform.config.security.CustomUserDetails;
import service.order.OrderService;
import service.subscription.SubscriptionService;
import service.user.UserService;

import java.util.List;

@Controller
@RequestMapping("/{lang}/cabinet")
@RequiredArgsConstructor
@Slf4j
public class UserCabinetController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final OrderService orderService;

    /**
     * Головна сторінка кабінету - dashboard
     */
    @GetMapping
    public String dashboard(
            @PathVariable String lang,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        User user = userService.findById(userDetails.getId());
        UserSubscription activeSub = subscriptionService.getActiveSubscription(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("activeSubscription", activeSub);
        model.addAttribute("hasActiveSubscription", activeSub != null);

        return "cabinet/dashboard";
    }

    /**
     * Сторінка підписок
     */
    @GetMapping("/subscription")
    public String subscription(
            @PathVariable String lang,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        User user = userService.findById(userDetails.getId());
        UserSubscription activeSub = subscriptionService.getActiveSubscription(user.getId());
        List<SubscriptionPlan> plans = subscriptionService.getActivePlans();

        model.addAttribute("user", user);
        model.addAttribute("activeSubscription", activeSub);
        model.addAttribute("subscriptionPlans", plans);
        model.addAttribute("lang", lang);

        return "cabinet/subscription";
    }

    /**
     * Історія підписок
     */
    @GetMapping("/subscription/history")
    public String subscriptionHistory(
            @PathVariable String lang,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        User user = userService.findById(userDetails.getId());
        List<UserSubscription> history = subscriptionService.getUserSubscriptionHistory(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("subscriptionHistory", history);

        return "cabinet/subscription-history";
    }

    /**
     * Скасувати підписку (припинити авто-продовження)
     */
    @PostMapping("/subscription/{subscriptionId}/cancel")
    @ResponseBody
    public String cancelSubscription(
            @PathVariable String lang,
            @PathVariable Long subscriptionId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            // Перевірити що підписка належить користувачу
            UserSubscription sub = subscriptionService.getActiveSubscription(userDetails.getId());
            if (sub == null || !sub.getId().equals(subscriptionId)) {
                return "{\"success\": false, \"message\": \"Access denied\"}";
            }

            subscriptionService.cancelSubscription(subscriptionId);
            return "{\"success\": true}";
        } catch (Exception e) {
            log.error("Error cancelling subscription", e);
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * Сторінка історії замовлень
     */
    @GetMapping("/orders")
    public String orders(
            @PathVariable String lang,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        User user = userService.findById(userDetails.getId());
        List<Order> orders = orderService.getUserOrders(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("orders", orders);

        return "cabinet/orders";
    }

    /**
     * Деталі замовлення
     */
    @GetMapping("/orders/{orderId}")
    public String orderDetails(
            @PathVariable String lang,
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        Order order = orderService.getOrder(orderId);

        // Перевірити що замовлення належить користувачу
        if (!order.getUser().getId().equals(userDetails.getId())) {
            return "redirect:/{lang}/cabinet/orders";
        }

        model.addAttribute("order", order);

        return "cabinet/order-details";
    }

    /**
     * Налаштування профілю
     */
    @GetMapping("/settings")
    public String settings(
            @PathVariable String lang,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        User user = userService.findByIdWithDetails(userDetails.getId());

        model.addAttribute("user", user);
        model.addAttribute("lang", lang);

        return "cabinet/settings";
    }

    /**
     * Оновити налаштування профілю
     */
    @PostMapping("/settings")
    public String updateSettings(
            @PathVariable String lang,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String name,
            Model model
    ) {
        try {
            User user = userService.findById(userDetails.getId());
            user.setName(name);
            userService.save(user);

            model.addAttribute("success", true);
            model.addAttribute("user", user);
        } catch (Exception e) {
            log.error("Error updating settings", e);
            model.addAttribute("error", true);
        }

        return "cabinet/settings";
    }
}