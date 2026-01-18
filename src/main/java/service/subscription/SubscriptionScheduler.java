package service.subscription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final SubscriptionService subscriptionService;

    @Scheduled(cron = "0 0 2 * * ?") // щодня о 02:00
    public void deactivateExpiredSubscriptions() {
        log.info("Starting expired subscriptions deactivation job");
        try {
            subscriptionService.deactivateExpiredSubscriptions();
        } catch (Exception e) {
            log.error("Error during expired subscriptions deactivation", e);
        }
    }
}
