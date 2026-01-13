package service.payment.provider;

import dto.payment.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.payment.PaymentProvider;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

/**
 * Інтеграція з WayForPay
 * Документація: https://wiki.wayforpay.com/
 */
@Service("wayforpay")
@Slf4j
public class WayForPayProvider implements PaymentProvider {

    @Value("${payment.wayforpay.merchant-account}")
    private String merchantAccount;

    @Value("${payment.wayforpay.merchant-domain}")
    private String merchantDomain;

    @Value("${payment.wayforpay.secret-key}")
    private String secretKey;

    @Value("${payment.wayforpay.api-url:https://secure.wayforpay.com/pay}")
    private String apiUrl;

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            log.info("Creating WayForPay payment for order {}", request.getOrderId());

            // Підготувати параметри
            String orderReference = "ORDER_" + request.getOrderId() + "_" +
                    LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            String orderDate = String.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            String amount = formatAmount(request.getAmount());

            // Згенерувати підпис
            String signature = generateSignature(
                    merchantAccount,
                    merchantDomain,
                    orderReference,
                    orderDate,
                    amount,
                    request.getCurrency().name(),
                    request.getDescription()
            );

            // Побудувати URL з параметрами (для форми або редіректу)
            String paymentUrl = buildPaymentUrl(
                    orderReference,
                    orderDate,
                    amount,
                    request.getCurrency().name(),
                    request.getDescription(),
                    request.getReturnUrl(),
                    signature,
                    request.getCustomerEmail(),
                    request.getLanguage()
            );

            return PaymentResponse.builder()
                    .success(true)
                    .paymentUrl(paymentUrl)
                    .orderId(orderReference)
                    .build();

        } catch (Exception e) {
            log.error("Error creating WayForPay payment", e);
            return PaymentResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentWebhookData handleWebhook(String webhookData, String receivedSignature) {
        try {
            // Парсинг JSON від WayForPay
            // TODO: Використати Jackson для парсингу

            // Перевірити підпис
            boolean signatureValid = verifySignature(webhookData, receivedSignature);

            if (!signatureValid) {
                log.error("Invalid WayForPay webhook signature");
                return PaymentWebhookData.builder()
                        .signatureValid(false)
                        .build();
            }

            // Витягти дані з webhook
            // TODO: Реалізувати парсинг конкретних полів

            return PaymentWebhookData.builder()
                    .orderId("extracted_order_id")
                    .transactionId("extracted_transaction_id")
                    .status("success")
                    .signatureValid(true)
                    .build();

        } catch (Exception e) {
            log.error("Error handling WayForPay webhook", e);
            return null;
        }
    }

    @Override
    public String checkPaymentStatus(String orderId) {
        // TODO: Реалізувати API запит до WayForPay для перевірки статусу
        return "PENDING";
    }

    @Override
    public String getProviderName() {
        return "WayForPay";
    }

    // === Helper Methods ===

    private String generateSignature(String... params) {
        try {
            String data = String.join(";", params);

            Mac hmac = Mac.getInstance("HmacMD5");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacMD5"
            );
            hmac.init(secretKeySpec);

            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);

        } catch (Exception e) {
            log.error("Error generating signature", e);
            return null;
        }
    }

    private boolean verifySignature(String data, String receivedSignature) {
        String calculatedSignature = generateSignature(data);
        return calculatedSignature != null && calculatedSignature.equals(receivedSignature);
    }

    private String buildPaymentUrl(
            String orderReference,
            String orderDate,
            String amount,
            String currency,
            String description,
            String returnUrl,
            String signature,
            String email,
            String language
    ) {
        // WayForPay використовує POST форму, тому тут генеруємо URL з параметрами
        // Або можна створити HTML форму яка автоматично сабмититься

        return String.format(
                "%s?merchantAccount=%s&merchantDomainName=%s&orderReference=%s" +
                        "&orderDate=%s&amount=%s&currency=%s&productName[]=%s" +
                        "&productCount[]=1&productPrice[]=%s&clientEmail=%s" +
                        "&language=%s&returnUrl=%s&merchantSignature=%s",
                apiUrl, merchantAccount, merchantDomain, orderReference,
                orderDate, amount, currency, description, amount, email,
                language, returnUrl, signature
        );
    }

    private String formatAmount(Long amountInCents) {
        return String.format("%.2f", amountInCents / 100.0);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
