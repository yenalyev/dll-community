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
import java.util.Base64;

/**
 * Інтеграція з Fondy (Portmone/CloudPayments)
 * Документація: https://docs.fondy.eu/
 */
@Service("fondy")
@Slf4j
public class FondyProvider implements PaymentProvider {

    @Value("${payment.fondy.merchant-id}")
    private String merchantId;

    @Value("${payment.fondy.secret-key}")
    private String secretKey;

    @Value("${payment.fondy.api-url:https://pay.fondy.eu/api/checkout/url/}")
    private String apiUrl;

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            log.info("Creating Fondy payment for order {}", request.getOrderId());

            // Підготувати параметри
            String orderId = "ORDER_" + request.getOrderId();
            String amount = String.valueOf(request.getAmount()); // В копійках

            // Згенерувати підпис
            String signature = generateSignature(
                    merchantId,
                    orderId,
                    amount,
                    request.getCurrency().name()
            );

            // TODO: Зробити HTTP POST запит до Fondy API
            // Отримати checkout_url у відповідь

            String paymentUrl = "https://pay.fondy.eu/merchants/" + merchantId +
                    "/checkout?order_id=" + orderId;

            return PaymentResponse.builder()
                    .success(true)
                    .paymentUrl(paymentUrl)
                    .orderId(orderId)
                    .build();

        } catch (Exception e) {
            log.error("Error creating Fondy payment", e);
            return PaymentResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentWebhookData handleWebhook(String webhookData, String receivedSignature) {
        try {
            // Парсинг JSON від Fondy
            boolean signatureValid = verifySignature(webhookData, receivedSignature);

            if (!signatureValid) {
                log.error("Invalid Fondy webhook signature");
                return PaymentWebhookData.builder()
                        .signatureValid(false)
                        .build();
            }

            // TODO: Витягти дані з webhook

            return PaymentWebhookData.builder()
                    .orderId("extracted_order_id")
                    .transactionId("extracted_transaction_id")
                    .status("success")
                    .signatureValid(true)
                    .build();

        } catch (Exception e) {
            log.error("Error handling Fondy webhook", e);
            return null;
        }
    }

    @Override
    public String checkPaymentStatus(String orderId) {
        // TODO: Реалізувати API запит до Fondy
        return "PENDING";
    }

    @Override
    public String getProviderName() {
        return "Fondy";
    }

    // === Helper Methods ===

    private String generateSignature(String... params) {
        try {
            String data = secretKey + "|" + String.join("|", params);

            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

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

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}