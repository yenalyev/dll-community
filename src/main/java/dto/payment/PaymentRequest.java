package dto.payment;

import entity.enums.Currency;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRequest {
    private Long orderId;
    private Long amount; // В копійках
    private Currency currency;
    private String customerEmail;
    private String customerName;
    private String description;
    private String returnUrl;
    private String callbackUrl;
    private String language; // uk, en, de
}
