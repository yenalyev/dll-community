package service.subscription;

import entity.enums.SubscriptionStatus;
import entity.lesson.Lesson;
import entity.order.UserSubscription;
import entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.OrderRepository;
import repository.UserSubscriptionRepository;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Сервіс для перевірки доступу до платних уроків
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonAccessService {

    private final OrderRepository orderRepository;
    private final UserSubscriptionRepository subscriptionRepository;

    // ========== ГОЛОВНІ МЕТОДИ ==========

    /**
     * Перевірити чи має користувач доступ до уроку
     *
     * @param user користувач (може бути null якщо не залогінений)
     * @param lesson урок
     * @return true якщо є доступ
     */
    public boolean hasAccessToLesson(User user, Lesson lesson) {

        // 1. FREE уроки доступні всім (навіть незалогіненим)
        if (lesson.getAccessLevel() == Lesson.AccessLevel.FREE) {
            log.debug("Lesson {} is FREE - access granted", lesson.getId());
            return true;
        }

        // 2. PAID уроки - потрібна авторизація
        if (user == null) {
            log.debug("User not authenticated - access denied to paid lesson {}",
                    lesson.getId());
            return false;
        }

        // 3. Перевірка активної підписки
        if (hasActiveSubscription(user)) {
            log.debug("User {} has active subscription - access granted to lesson {}",
                    user.getId(), lesson.getId());
            return true;
        }

        // 4. Перевірка чи придбав цей урок окремо
        if (hasPurchasedLesson(user, lesson)) {
            log.debug("User {} purchased lesson {} - access granted",
                    user.getId(), lesson.getId());
            return true;
        }

        log.debug("User {} has no access to paid lesson {}",
                user.getId(), lesson.getId());
        return false;
    }

    /**
     * Перевірити чи є у користувача активна підписка
     *
     * @param user користувач
     * @return true якщо є активна підписка
     */
    public boolean hasActiveSubscription(User user) {
        if (user == null) {
            return false;
        }

        return subscriptionRepository.hasActiveSubscription(
                user.getId(),
                LocalDateTime.now()
        );
    }

    /**
     * Перевірити чи користувач придбав конкретний урок
     *
     * @param user користувач
     * @param lesson урок
     * @return true якщо придбав
     */
    public boolean hasPurchasedLesson(User user, Lesson lesson) {
        if (user == null || lesson == null) {
            return false;
        }

        return orderRepository.existsCompletedOrderForLesson(
                user.getId(),
                lesson.getId()
        );
    }

    /**
     * Отримати активну підписку користувача
     *
     * @param user користувач
     * @return Optional з підпискою
     */
    public UserSubscription getActiveSubscription(User user) {
        if (user == null) {
            return null;
        }

        return subscriptionRepository.findActiveByUserId(
                user.getId(),
                LocalDateTime.now()
        ).orElse(null);
    }

    /**
     * Отримати причину відсутності доступу (для відображення в UI)
     *
     * @param user користувач
     * @param lesson урок
     * @return причина або null якщо є доступ
     */
    public AccessDenialReason getAccessDenialReason(User user, Lesson lesson) {

        // FREE урок - доступ є
        if (lesson.getAccessLevel() == Lesson.AccessLevel.FREE) {
            return null;
        }

        // Не залогінений
        if (user == null) {
            return AccessDenialReason.NOT_AUTHENTICATED;
        }

        // Є підписка - доступ є
        if (hasActiveSubscription(user)) {
            return null;
        }

        // Купив урок - доступ є
        if (hasPurchasedLesson(user, lesson)) {
            return null;
        }

        // Перевіряємо чи була підписка раніше
        boolean hadSubscription = subscriptionRepository.existsByUserIdAndStatusIn(
                user.getId(),
                Arrays.asList(
                        SubscriptionStatus.EXPIRED,
                        SubscriptionStatus.CANCELED
                )
        );

        if (hadSubscription) {
            return AccessDenialReason.SUBSCRIPTION_EXPIRED;
        }

        return AccessDenialReason.NO_SUBSCRIPTION;
    }

    // ========== ENUM ДЛЯ ПРИЧИН ВІДМОВИ ==========

    /**
     * Причини відмови в доступі
     */
    public enum AccessDenialReason {
        /** Користувач не залогінений */
        NOT_AUTHENTICATED,

        /** Немає підписки взагалі */
        NO_SUBSCRIPTION,

        /** Підписка була, але закінчилась */
        SUBSCRIPTION_EXPIRED,

        /** Урок не придбаний окремо */
        LESSON_NOT_PURCHASED
    }
}