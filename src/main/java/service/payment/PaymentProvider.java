package service.payment;

import dto.payment.PaymentRequest;
import dto.payment.PaymentResponse;
import dto.payment.PaymentWebhookData;

/**
 * Абстракція для роботи з різними платіжними системами
 */
public interface PaymentProvider {

    /**
     * Створити платіж та отримати URL для редіректу
     */
    PaymentResponse createPayment(PaymentRequest request);

    /**
     * Обробити webhook від платіжної системи
     */
    PaymentWebhookData handleWebhook(String webhookData, String signature);

    /**
     * Перевірити статус платежу
     */
    String checkPaymentStatus(String orderId);

    /**
     * Назва провайдера
     */
    String getProviderName();
}
