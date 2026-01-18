package controller;

import dto.payment.PaymentResponse;
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
import java.util.Map;

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
            if (userDetails == null) {
                return "redirect:/{lang}/login?redirect=subscription";
            }

            User user = userService.findByIdWithDetails(userDetails.getId());
            Order order = orderService.createSubscriptionOrder(user, planId, currency);

            log.info("Order {} created, generating payment form", order.getId());

            PaymentResponse paymentResponse = paymentService.createPayment(order, lang);

            if (!paymentResponse.isSuccess()) {
                log.error("Failed to create payment: {}", paymentResponse.getError());
                model.addAttribute("error", true);
                return "redirect:/{lang}/subscription";
            }

            // ⬇️ ПЕРЕДАТИ ДАНІ В MODEL
            model.addAttribute("paymentUrl", paymentResponse.getPaymentUrl());
            model.addAttribute("formData", paymentResponse.getFormData());

            log.info("Rendering payment form for order {}", order.getId());

            return "payment/redirect";

        } catch (Exception e) {
            log.error("Error creating subscription order", e);
            model.addAttribute("error", true);
            return "redirect:/{lang}/subscription";
        }
    }

    /**
     * Callback після успішної оплати (GET і POST)
     */
    @RequestMapping(
            value = "/success/{orderId}",
            method = {RequestMethod.GET, RequestMethod.POST}
    )
    public String paymentSuccess(
            @PathVariable String lang,
            @PathVariable Long orderId,
            @RequestParam(required = false) Map<String, String> params,
            Model model
    ) {
        log.info("=== Payment return for order {} ===", orderId);
        log.info("Params from WayForPay: {}", params);

        Order order = orderService.getOrder(orderId);

        // Обробка параметрів від WayForPay
        if (params != null && !params.isEmpty() && params.containsKey("transactionStatus")) {
            String status = params.get("transactionStatus");
            String reasonCode = params.get("reasonCode");

            log.info("Payment status: {}, reasonCode: {}", status, reasonCode);

            if ("Approved".equals(status) && "1100".equals(reasonCode)) {
                log.info("✅ Payment approved for order {}", orderId);

                try {
                    String authCode = params.getOrDefault("authCode", "");
                    orderService.completeOrder(orderId, "wayforpay", authCode);
                    log.info("Order {} completed successfully", orderId);

                    // Перечитуємо оновлене замовлення
                    order = orderService.getOrder(orderId);
                } catch (Exception e) {
                    log.error("Error completing order {}", orderId, e);
                }
            } else {
                log.warn("❌ Payment not approved: status={}, reason={}", status, reasonCode);
            }
        }

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

