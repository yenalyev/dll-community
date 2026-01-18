package service.payment.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.payment.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.payment.PaymentProvider;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Інтеграція з WayForPay
 * Документація: https://wiki.wayforpay.com/
 *
 * TEST MODE: Всі платежі автоматично повертаються
 */
@Service("wayforpay")
@Slf4j
public class WayForPayProvider implements PaymentProvider {

    private final ObjectMapper objectMapper = new ObjectMapper();

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
            String orderReference = "DLL_" + request.getOrderId() + "_" + Instant.now().getEpochSecond();
            String orderDate = String.valueOf(Instant.now().getEpochSecond());
            String amount = formatAmount(request.getAmount());
            String productName = request.getDescription();

            // Перевірка обов'язкових полів
            if (request.getCustomerEmail() == null || request.getCustomerEmail().isEmpty()) {
                throw new IllegalArgumentException("Customer email is required");
            }

            log.debug("Payment params: merchant={}, domain={}, order={}, amount={}, currency={}",
                    merchantAccount, merchantDomain, orderReference, amount, request.getCurrency());

            // Згенерувати підпис для платежу
            String signature = generatePaymentSignature(
                    merchantAccount,
                    merchantDomain,
                    orderReference,
                    orderDate,
                    amount,
                    request.getCurrency().name(),
                    productName,
                    "1", // productCount
                    amount  // productPrice
            );

            log.debug("Generated signature: {}", signature);

            // ⬇️ СТВОРИТИ MAP з даними для POST форми
            Map<String, String> formData = new LinkedHashMap<>();
            formData.put("merchantAccount", merchantAccount);
            formData.put("merchantDomainName", merchantDomain);
            formData.put("orderReference", orderReference);
            formData.put("orderDate", orderDate);
            formData.put("amount", amount);
            formData.put("currency", request.getCurrency().name());
            formData.put("productName[]", productName);
            formData.put("productCount[]", "1");
            formData.put("productPrice[]", amount);
            formData.put("clientEmail", request.getCustomerEmail());

            if (request.getCustomerName() != null && !request.getCustomerName().isEmpty()) {
                formData.put("clientFirstName", request.getCustomerName());
            }

            if (request.getReturnUrl() != null && !request.getReturnUrl().isEmpty()) {
                formData.put("returnUrl", request.getReturnUrl());
            }

            if (request.getCallbackUrl() != null && !request.getCallbackUrl().isEmpty()) {
                formData.put("serviceUrl", request.getCallbackUrl());
            }

            formData.put("language", mapLanguage(request.getLanguage()));
            formData.put("merchantSignature", signature);

            log.info("Payment form data prepared for order {}: {}", request.getOrderId(), orderReference);

            return PaymentResponse.builder()
                    .success(true)
                    .paymentUrl(apiUrl) // URL форми для POST
                    .orderId(orderReference)
                    .formData(formData) // ⬅️ Дані для hidden полів
                    .build();

        } catch (Exception e) {
            log.error("Error creating WayForPay payment for order {}", request.getOrderId(), e);
            return PaymentResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentWebhookData handleWebhook(String webhookData, String receivedSignature) {
        try {
            log.info("Processing WayForPay webhook");
            log.debug("Webhook data: {}", webhookData);

            // Парсинг JSON (який ми створили з Map у контролері)
            JsonNode json = objectMapper.readTree(webhookData);

            String orderReference = json.get("orderReference").asText();
            String transactionStatus = json.get("transactionStatus").asText();
            String reasonCode = json.get("reasonCode").asText();
            String amount = json.get("amount").asText();
            String currency = json.get("currency").asText();
            String authCode = json.has("authCode") ? json.get("authCode").asText() : "";
            String cardPan = json.has("cardPan") ? json.get("cardPan").asText() : "";

            log.info("Webhook params: order={}, status={}, reason={}",
                    orderReference, transactionStatus, reasonCode);

            // Перевірити підпис
            boolean signatureValid = verifyWebhookSignature(
                    merchantAccount,
                    orderReference,
                    amount,
                    currency,
                    authCode,
                    cardPan,
                    transactionStatus,
                    reasonCode,
                    receivedSignature
            );

            if (!signatureValid) {
                log.error("❌ Invalid signature for order {}", orderReference);
                return PaymentWebhookData.builder()
                        .signatureValid(false)
                        .build();
            }

            // Статус
            String status = mapTransactionStatus(transactionStatus, reasonCode);

            log.info("✅ Webhook verified: order={}, status={}", orderReference, status);

            return PaymentWebhookData.builder()
                    .orderId(orderReference)
                    .transactionId(authCode)
                    .status(status)
                    .amount((long)(Double.parseDouble(amount) * 100)) // в копійки
                    .currency(currency)
                    .signatureValid(true)
                    .build();

        } catch (Exception e) {
            log.error("Error handling webhook", e);
            return PaymentWebhookData.builder()
                    .signatureValid(false)
                    .build();
        }
    }

    @Override
    public String checkPaymentStatus(String orderId) {
        // TODO: Можна реалізувати через API CHECK_STATUS якщо потрібно
        log.info("Checking payment status for order: {}", orderId);
        return "PENDING";
    }

    @Override
    public String getProviderName() {
        return "WayForPay";
    }

    // ========== HELPER METHODS ==========

    /**
     * Генерація підпису для створення платежу
     */
    private String generatePaymentSignature(String... params) {
        try {
            // Об'єднуємо параметри через ;
            String data = String.join(";", params);

            log.debug("Signature data: {}", data);

            // HMAC MD5
            Mac hmac = Mac.getInstance("HmacMD5");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacMD5"
            );
            hmac.init(secretKeySpec);

            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);

        } catch (Exception e) {
            log.error("Error generating payment signature", e);
            throw new RuntimeException("Signature generation failed", e);
        }
    }

    /**
     * Перевірка підпису webhook
     */
    private boolean verifyWebhookSignature(String... params) {
        try {
            // Останній параметр - це отриманий підпис
            String receivedSignature = params[params.length - 1];

            // Генеруємо підпис з усіх параметрів крім останнього
            String[] signatureParams = new String[params.length - 1];
            System.arraycopy(params, 0, signatureParams, 0, params.length - 1);

            String calculatedSignature = generatePaymentSignature(signatureParams);

            boolean isValid = calculatedSignature.equalsIgnoreCase(receivedSignature);

            if (!isValid) {
                log.warn("Signature mismatch! Calculated: {}, Received: {}",
                        calculatedSignature, receivedSignature);
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    /**
     * Маппінг статусів WayForPay на наші внутрішні статуси
     */
    private String mapTransactionStatus(String transactionStatus, String reasonCode) {
        // WayForPay статуси:
        // Approved - успішно
        // Declined - відхилено
        // Refunded - повернено
        // Pending - в обробці
        // Expired - прострочено

        switch (transactionStatus.toLowerCase()) {
            case "approved":
                return "success";
            case "declined":
            case "expired":
                return "failed";
            case "refunded":
                return "refunded";
            case "pending":
            default:
                return "pending";
        }
    }

    /**
     * Побудова URL для оплати
     */
    private String buildPaymentUrl(
            String orderReference,
            String orderDate,
            String amount,
            String currency,
            String productName,
            String returnUrl,
            String serviceUrl,
            String signature,
            String email,
            String clientName,
            String language
    ) {
        // WayForPay приймає GET параметри для автоматичного заповнення форми
        StringBuilder url = new StringBuilder(apiUrl);
        url.append("?merchantAccount=").append(merchantAccount);
        url.append("&merchantDomainName=").append(merchantDomain);
        url.append("&orderReference=").append(orderReference);
        url.append("&orderDate=").append(orderDate);
        url.append("&amount=").append(amount);
        url.append("&currency=").append(currency);
        url.append("&productName[]=").append(urlEncode(productName));
        url.append("&productCount[]=1");
        url.append("&productPrice[]=").append(amount);
        url.append("&clientEmail=").append(urlEncode(email));

        if (clientName != null && !clientName.isEmpty()) {
            url.append("&clientFirstName=").append(urlEncode(clientName));
        }

        if (returnUrl != null && !returnUrl.isEmpty()) {
            url.append("&returnUrl=").append(urlEncode(returnUrl));
        }

        if (serviceUrl != null && !serviceUrl.isEmpty()) {
            url.append("&serviceUrl=").append(urlEncode(serviceUrl));
        }

        // Мова інтерфейсу WayForPay (ua, ru, en)
        String wfpLang = mapLanguage(language);
        url.append("&language=").append(wfpLang);

        url.append("&merchantSignature=").append(signature);

        return url.toString();
    }

    /**
     * Маппінг наших кодів мов на коди WayForPay
     */
    private String mapLanguage(String lang) {
        if (lang == null) return "ua";

        switch (lang.toLowerCase()) {
            case "uk":
            case "ua":
                return "ua";
            case "en":
                return "en";
            case "de":
            case "ru":
                return "ru"; // WayForPay не підтримує німецьку, використовуємо російську
            default:
                return "ua";
        }
    }

    /**
     * Форматування суми (копійки -> гривні)
     */
    private String formatAmount(Long amountInCents) {
        return String.format("%.2f", amountInCents / 100.0);
    }

    /**
     * Конвертація byte[] в hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * URL encoding для параметрів
     */
    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }
}