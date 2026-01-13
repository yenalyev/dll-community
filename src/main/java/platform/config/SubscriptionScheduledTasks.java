package platform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import service.subscription.SubscriptionService;

/**
 * Scheduled tasks для автоматичної обробки підписок
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduledTasks {

    private final SubscriptionService subscriptionService;

    /**
     * Деактивувати прострочені підписки (кожні 6 годин)
     */
    @Scheduled(cron = "0 0 */6 * * *")
    public void deactivateExpiredSubscriptions() {
        log.info("Running scheduled task: deactivateExpiredSubscriptions");
        try {
            subscriptionService.deactivateExpiredSubscriptions();
            log.info("Successfully deactivated expired subscriptions");
        } catch (Exception e) {
            log.error("Error deactivating expired subscriptions", e);
        }
    }

    /**
     * Відправити нагадування про закінчення підписки (щодня о 10:00)
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void sendExpirationReminders() {
        log.info("Running scheduled task: sendExpirationReminders");
        try {
            // TODO: Реалізувати відправку email нагадувань
            // subscriptionService.sendExpirationReminders();
            log.info("Successfully sent expiration reminders");
        } catch (Exception e) {
            log.error("Error sending expiration reminders", e);
        }
    }
}
