package entity.enums;

public enum OrderStatus {
    PENDING,     // Очікує оплати
    PROCESSING,  // В обробці
    COMPLETED,   // Завершено
    FAILED,      // Помилка
    CANCELLED,   // Скасовано
    REFUNDED     // Повернено кошти
}
