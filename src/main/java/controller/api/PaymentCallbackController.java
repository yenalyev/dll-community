package controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.payment.PaymentService;

import java.util.Map;

/**
 * API контролер для обробки webhooks від платіжних систем
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    /**
     * Webhook від WayForPay
     *
     * WayForPay відправляє POST запит з JSON тілом
     * merchantSignature може бути як в header, так і в JSON
     */
    @PostMapping("/callback/wayforpay")
    public ResponseEntity<String> wayforpayCallback(
            @RequestParam Map<String, String> params  // ← Замість @RequestBody
    ) {
        log.info("=== Received WayForPay callback ===");
        log.info("All params: {}", params);
        log.info("merchantSignature: {}", params.get("merchantSignature"));
        log.info("orderReference: {}", params.get("orderReference"));
        log.info("transactionStatus: {}", params.get("transactionStatus"));
        log.info("reasonCode: {}", params.get("reasonCode"));

        try {
            // Конвертуємо Map в JSON для PaymentService
            String requestBody = objectMapper.writeValueAsString(params);
            String signature = params.get("merchantSignature");

            boolean success = paymentService.handlePaymentCallback(
                    "wayforpay",
                    requestBody,
                    signature
            );

            String orderRef = params.getOrDefault("orderReference", "");
            long currentTime = System.currentTimeMillis() / 1000;

            if (success) {
                log.info("WayForPay callback processed successfully for order: {}", orderRef);
                String response = String.format(
                        "{\"orderReference\":\"%s\",\"status\":\"accept\",\"time\":%d}",
                        orderRef, currentTime
                );
                return ResponseEntity.ok(response);
            } else {
                log.error("WayForPay callback failed for order: {}", orderRef);
                String response = String.format(
                        "{\"orderReference\":\"%s\",\"status\":\"decline\",\"time\":%d}",
                        orderRef, currentTime
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            log.error("Error processing WayForPay callback", e);
            String response = String.format(
                    "{\"orderReference\":\"\",\"status\":\"decline\",\"time\":%d}",
                    System.currentTimeMillis() / 1000
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Webhook від Fondy (для майбутнього використання)
     */
    @PostMapping("/callback/fondy")
    public ResponseEntity<String> fondyCallback(
            @RequestBody String requestBody,
            @RequestHeader(value = "signature", required = false) String signature
    ) {
        log.info("=== Received Fondy callback ===");
        log.debug("Request body: {}", requestBody);

        try {
            boolean success = paymentService.handlePaymentCallback(
                    "fondy",
                    requestBody,
                    signature
            );

            if (success) {
                return ResponseEntity.ok("{\"status\":\"success\"}");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"status\":\"error\",\"message\":\"Invalid signature\"}");
            }

        } catch (Exception e) {
            log.error("Error processing Fondy callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Перевірка статусу платежу (для polling з фронтенду)
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<String> checkPaymentStatus(@PathVariable Long orderId) {
        try {
            // TODO: Реалізувати перевірку через OrderService
            log.info("Checking payment status for order: {}", orderId);
            return ResponseEntity.ok("{\"status\":\"pending\"}");
        } catch (Exception e) {
            log.error("Error checking payment status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":\"error\"}");
        }
    }
}