package dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private boolean success;
    private String paymentUrl; // URL для редіректу користувача
    private String orderId;
    private String error;
}
