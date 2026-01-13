package dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentWebhookData {
    private String orderId;
    private String transactionId;
    private String status; // success, failed, pending
    private Long amount;
    private String currency;
    private boolean signatureValid;
}
