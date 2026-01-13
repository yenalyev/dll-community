package controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.payment.PaymentService;


/**
 * API контролер для обробки webhooks від платіжних систем
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackController {

    private final PaymentService paymentService;

    /**
     * Webhook від WayForPay
     */
    @PostMapping("/callback/wayforpay")
    public ResponseEntity<String> wayforpayCallback(
            @RequestBody String requestBody,
            @RequestHeader(value = "merchantSignature", required = false) String signature
    ) {
        log.info("Received WayForPay callback");
        log.debug("Request body: {}", requestBody);

        try {
            boolean success = paymentService.handlePaymentCallback(
                    "wayforpay",
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
            log.error("Error processing WayForPay callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Webhook від Fondy
     */
    @PostMapping("/callback/fondy")
    public ResponseEntity<String> fondyCallback(
            @RequestBody String requestBody,
            @RequestHeader(value = "signature", required = false) String signature
    ) {
        log.info("Received Fondy callback");
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
            return ResponseEntity.ok("{\"status\":\"pending\"}");
        } catch (Exception e) {
            log.error("Error checking payment status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":\"error\"}");
        }
    }
}
