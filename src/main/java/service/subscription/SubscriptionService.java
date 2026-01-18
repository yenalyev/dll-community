// ===== SubscriptionService.java =====

package service.subscription;

import entity.enums.SubscriptionStatus;
import entity.order.*;
import entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.OrderRepository;
import repository.SubscriptionPlanRepository;
import repository.UserRepository;
import repository.UserSubscriptionRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    /**
     * Отримати активну підписку користувача
     */
    public UserSubscription getActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveByUserId(userId, LocalDateTime.now()).orElse(null);
    }

    /**
     * Перевірити чи має користувач активну підписку
     */
    public boolean hasActiveSubscription(Long userId) {
        return subscriptionRepository.hasActiveSubscription(userId, LocalDateTime.now());
    }

    /**
     * Створити нову підписку після успішної оплати
     */
    @Transactional
    public UserSubscription createSubscription(User user, SubscriptionPlan plan, Order order) {
        log.info("Creating subscription for user {} with plan {}", user.getId(), plan.getId());

        // Перевірити чи немає активної підписки
        UserSubscription existingActive = getActiveSubscription(user.getId());
        if (existingActive != null) {
            // Якщо є активна - продовжити її
            return extendSubscription(existingActive, plan);
        }

        // Створити нову підписку
        UserSubscription subscription = new UserSubscription();
        subscription.setUser(user);
        subscription.setSubscriptionPlan(plan);
        subscription.setOrder(order);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusDays(plan.getDurationInDays()));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setAutoRenew(false); // За замовчуванням без авто-продовження

        return subscriptionRepository.save(subscription);
    }

    /**
     * Продовжити існуючу підписку
     */
    @Transactional
    public UserSubscription extendSubscription(UserSubscription subscription, SubscriptionPlan newPlan) {
        log.info("Extending subscription {} with plan {}", subscription.getId(), newPlan.getId());

        LocalDateTime newEndDate;

        // Якщо підписка ще активна - додаємо час до кінця
        if (subscription.getEndDate().isAfter(LocalDateTime.now())) {
            newEndDate = subscription.getEndDate().plusDays(newPlan.getDurationInDays());
        } else {
            // Якщо вже закінчилась - починаємо з поточної дати
            newEndDate = LocalDateTime.now().plusDays(newPlan.getDurationInDays());
        }

        subscription.setEndDate(newEndDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setSubscriptionPlan(newPlan);

        return subscriptionRepository.save(subscription);
    }

    /**
     * Скасувати підписку (припинити авто-продовження)
     */
    @Transactional
    public void cancelSubscription(Long subscriptionId) {
        UserSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setAutoRenew(false);
        subscription.setCancelledAt(LocalDateTime.now());

        log.info("Subscription {} cancelled by user", subscriptionId);
        subscriptionRepository.save(subscription);
    }

    /**
     * Деактивувати прострочені підписки (запускається по cron)
     */
    @Transactional
    public void deactivateExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        int gracePeriodDays = 5; // можна винести в application.properties
        LocalDateTime gracePeriodEnd = now.minusDays(gracePeriodDays);

        List<UserSubscription> expiredSubs =
                subscriptionRepository.findExpiredSubscriptions(now, gracePeriodEnd);

        if (expiredSubs.isEmpty()) {
            log.info("No expired subscriptions found");
            return;
        }

        for (UserSubscription sub : expiredSubs) {
            String reason = sub.getAutoRenew()
                    ? "failed renewal after grace period"
                    : "expired without auto-renewal";

            sub.setStatus(SubscriptionStatus.EXPIRED);
            sub.setUpdatedAt(now);

            log.info("Deactivated subscription {} for user {} ({})",
                    sub.getId(), sub.getUser().getId(), reason);

            // Опціонально: відправити email про деактивацію
            // emailService.sendSubscriptionExpiredEmail(sub);
        }

        subscriptionRepository.saveAll(expiredSubs);
        log.info("Deactivated {} expired subscriptions", expiredSubs.size());
    }

    /**
     * Отримати всі підписки користувача (історію)
     */
    public Optional<List<UserSubscription>> getUserSubscriptionHistory(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    /**
     * Отримати всі доступні плани підписок
     */
    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getActivePlans() {
        List<SubscriptionPlan> plans = planRepository.findByIsActiveTrue();

        if (!plans.isEmpty()) {
            // Дозавантажуємо translations
            planRepository.loadTranslations(plans);
        }

        return plans;
    }

    /**
     * Отримати план по ID
     */
    public SubscriptionPlan getPlanById(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
    }

    /**
     * Отримати переклад плану для мови
     */
    public SubscriptionPlanTranslation getPlanTranslation(SubscriptionPlan plan, String lang) {
        return plan.getTranslations().stream()
                .filter(t -> t.getLang().equals(lang))
                .findFirst()
                .orElse(null);
    }

    /**
     * Отримати ціну плану в валюті
     */
    public Long getPlanPrice(SubscriptionPlan plan, entity.enums.Currency currency) {
        return plan.getPrices().stream()
                .filter(p -> p.getCurrency() == currency)
                .findFirst()
                .map(SubscriptionPlanPrice::getAmount)
                .orElse(null);
    }
}


