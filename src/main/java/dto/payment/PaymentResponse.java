package dto.payment;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PaymentResponse {
    private boolean success;
    private String paymentUrl; // URL для редіректу користувача
    private String orderId;
    private String error;

    // ⬇️ НОВІ ПОЛЯ для POST форми
    private Map<String, String> formData; // Дані для hidden полів
}
