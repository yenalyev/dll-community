package service.payment;

import dto.payment.*;
import entity.order.Order;
import entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.order.OrderService;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final Map<String, PaymentProvider> paymentProviders;
    private final OrderService orderService;

    @Value("${payment.default-provider:wayforpay}")
    private String defaultProvider;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Створити платіж для замовлення
     */
    public PaymentResponse createPayment(Order order, String lang) {
        PaymentProvider provider = getProvider(defaultProvider);

        // Правильні URLs з підстановкою мови
        String returnUrl = baseUrl + "/" + lang + "/subscription/success/" + order.getId();
        String callbackUrl = baseUrl + "/api/payment/callback/" + defaultProvider;

        PaymentRequest request = PaymentRequest.builder()
                .orderId(order.getId())
                .amount(order.getTotalAmount())
                .currency(order.getCurrency())
                .customerEmail(getUserEmail(order.getUser()))
                .customerName(order.getUser().getName())
                .description(getOrderDescription(order, lang))
                .returnUrl(returnUrl)
                .callbackUrl(callbackUrl)
                .language(lang)
                .build();

        log.debug("Creating payment with returnUrl: {}, callbackUrl: {}", returnUrl, callbackUrl);

        return provider.createPayment(request);
    }

    /**
     * Обробити webhook від платіжної системи
     */
    public boolean handlePaymentCallback(String providerName, String data, String signature) {
        try {
            PaymentProvider provider = getProvider(providerName);
            PaymentWebhookData webhookData = provider.handleWebhook(data, signature);

            if (webhookData == null || !webhookData.isSignatureValid()) {
                log.error("Invalid webhook signature from {}", providerName);
                return false;
            }

            // Якщо платіж успішний - завершити замовлення
            if ("success".equals(webhookData.getStatus())) {
                Long orderId = extractOrderId(webhookData.getOrderId());
                orderService.completeOrder(orderId, providerName, webhookData.getTransactionId());
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("Error handling payment callback", e);
            return false;
        }
    }

    // === Helper Methods ===

    private PaymentProvider getProvider(String name) {
        PaymentProvider provider = paymentProviders.get(name);
        if (provider == null) {
            throw new RuntimeException("Payment provider not found: " + name);
        }
        return provider;
    }

    private String getUserEmail(User user) {
        // Отримати primary email користувача
        return user.getEmails().stream()
                .filter(e -> e.getIsPrimary())
                .findFirst()
                .map(e -> e.getEmail())
                .orElse(null);
    }

    private String getOrderDescription(Order order, String lang) {
        switch (lang) {
            case "uk": return "Підписка DLL Community";
            case "de": return "DLL Community Abonnement";
            default: return "DLL Community Subscription";
        }
    }

    private Long extractOrderId(String orderReference) {
        // Витягти ID з формату "ORDER_123_timestamp"
        String[] parts = orderReference.split("_");
        return Long.parseLong(parts[1]);
    }
}
