package controller;

import entity.enums.Currency;
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
import service.payment.PaymentService;
import service.subscription.SubscriptionService;
import service.user.UserService;

import java.util.List;

@Controller
@RequestMapping("/{lang}/subscription")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final UserService userService;

    /**
     * Сторінка з тарифними планами
     */
    @GetMapping
    public String subscriptionPlans(
            @PathVariable String lang,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        List<SubscriptionPlan> plans = subscriptionService.getActivePlans();

        // Якщо користувач авторизований - показати активну підписку
        if (userDetails != null) {
            UserSubscription activeSub = subscriptionService.getActiveSubscription(userDetails.getId());
            model.addAttribute("activeSubscription", activeSub);
        }

        model.addAttribute("subscriptionPlans", plans);

        model.addAttribute("lang", lang);

        return "subscription/plans";
    }

    /**
     * Початок процесу покупки підписки
     */
    @PostMapping("/purchase/{planId}")
    public String purchaseSubscription(
            @PathVariable String lang,
            @PathVariable Long planId,
            @RequestParam(defaultValue = "UAH") Currency currency,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        try {
            // Перевірити авторизацію
            if (userDetails == null) {
                return "redirect:/{lang}/login?redirect=subscription";
            }

            User user = userService.findById(userDetails.getId());
            //SubscriptionPlan plan = subscriptionService.getPlanById(planId);

            // Створити замовлення
            Order order = orderService.createSubscriptionOrder(user, planId, currency);

            // Перенаправити на сторінку оплати
            return "redirect:/{lang}/payment/subscription/" + order.getId();

        } catch (Exception e) {
            log.error("Error creating subscription order", e);
            model.addAttribute("error", true);
            return "redirect:/{lang}/subscription";
        }
    }

    /**
     * Callback після успішної оплати
     */
    @GetMapping("/success/{orderId}")
    public String paymentSuccess(
            @PathVariable String lang,
            @PathVariable Long orderId,
            Model model
    ) {
        Order order = orderService.getOrder(orderId);

        model.addAttribute("order", order);
        model.addAttribute("subscription", order.getSubscription());

        return "subscription/success";
    }

    /**
     * Callback при помилці оплати
     */
    @GetMapping("/failed/{orderId}")
    public String paymentFailed(
            @PathVariable String lang,
            @PathVariable Long orderId,
            Model model
    ) {
        Order order = orderService.getOrder(orderId);

        model.addAttribute("order", order);

        return "subscription/failed";
    }
}

