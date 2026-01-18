package repository;

import entity.enums.SubscriptionStatus;
import entity.order.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    /**
     * Знайти активну підписку користувача
     *
     * @param userId ID користувача
     * @return Optional з підпискою якщо є
     */
    @Query("SELECT us FROM UserSubscription us " +
            "WHERE us.user.id = :userId " +
            "AND us.status = 'ACTIVE' " +
            "AND us.endDate > :now " +
            "ORDER BY us.endDate DESC")
    Optional<UserSubscription> findActiveByUserId(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    /**
     * Знайти підписку користувача незалежно від статусу
     *
     * @param userId ID користувача
     * @return Optional з підпискою
     */
    Optional<List<UserSubscription>> findByUserId(Long userId);

    /**
     * Перевірити чи є у користувача активна підписка
     *
     * @param userId ID користувача
     * @param now поточний час
     * @return true якщо є активна підписка
     */
    @Query("SELECT CASE WHEN COUNT(us) > 0 THEN true ELSE false END " +
            "FROM UserSubscription us " +
            "WHERE us.user.id = :userId " +
            "AND us.status = 'ACTIVE' " +
            "AND us.endDate > :now")
    boolean hasActiveSubscription(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    /**
     * Знайти всі підписки що потребують автопродовження
     * Використовується для cron job
     *
     * @param now поточний час
     * @return список підписок для продовження
     */
    @Query("SELECT us FROM UserSubscription us " +
            "WHERE us.autoRenew = TRUE " +
            "AND us.status = 'ACTIVE' " +
            "AND us.nextBillingDate <= :now " +
            "AND us.cancelledAt IS NULL")
    List<UserSubscription> findSubscriptionsForRenewal(@Param("now") LocalDateTime now);


    /**
     * Знайти всі підписки що закінчилися
     * Використовується для cron job
     *
     * @param now поточний час
     * @return список підписок для продовження
     */
    @Query("SELECT us FROM UserSubscription us " +
            "WHERE us.status = 'ACTIVE' " +
            "AND us.nextBillingDate <= :now " +
            "AND us.cancelledAt IS NULL")
    List<UserSubscription> findAllExpiredSubscriptions(@Param("now") LocalDateTime now);

    /**
     * Знайти підписки що скоро закінчуються (для нагадувань)
     *
     * @param from початок періоду
     * @param to кінець періоду
     * @return список підписок
     */
    @Query("SELECT us FROM UserSubscription us " +
            "WHERE us.status = 'ACTIVE' " +
            "AND us.endDate BETWEEN :from AND :to " +
            "AND us.cancelledAt IS NULL")
    List<UserSubscription> findExpiringSubscriptions(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /**
     * Перевірити чи була у користувача підписка раніше
     *
     * @param userId ID користувача
     * @param statuses список статусів
     * @return true якщо була підписка
     */
    boolean existsByUserIdAndStatusIn(Long userId, List<SubscriptionStatus> statuses);

    /**
     * Знайти підписки БЕЗ автопродовження що закінчилися
     * Використовується для негайної деактивації
     *
     * @param now поточний час
     * @return список підписок для деактивації
     */
    @Query("SELECT us FROM UserSubscription us " +
            "WHERE us.status = 'ACTIVE' " +
            "AND us.autoRenew = FALSE " +
            "AND us.nextBillingDate <= :now " +
            "AND us.cancelledAt IS NULL")
    List<UserSubscription> findExpiredWithoutAutoRenew(@Param("now") LocalDateTime now);

    /**
     * Знайти підписки З автопродовженням які прострочені більше grace period
     * Використовується для деактивації після невдалого продовження
     *
     * @param gracePeriodEnd дата після якої закінчується grace period (now - 3-5 днів)
     * @return список підписок для деактивації
     */
    @Query("SELECT us FROM UserSubscription us " +
            "WHERE us.status = 'ACTIVE' " +
            "AND us.autoRenew = TRUE " +
            "AND us.nextBillingDate <= :gracePeriodEnd " +
            "AND us.cancelledAt IS NULL")
    List<UserSubscription> findExpiredAfterGracePeriod(@Param("gracePeriodEnd") LocalDateTime gracePeriodEnd);

    /**
     * Знайти підписки в grace period (для відправки нагадувань)
     * Підписки з автопродовженням, які прострочені але ще в межах grace period
     *
     * @param now поточний час
     * @param gracePeriodEnd межа grace period
     * @return список підписок для нагадувань
     */
    @Query("SELECT us FROM UserSubscription us " +
            "WHERE us.status = 'ACTIVE' " +
            "AND us.autoRenew = TRUE " +
            "AND us.nextBillingDate <= :now " +
            "AND us.nextBillingDate > :gracePeriodEnd " +
            "AND us.cancelledAt IS NULL")
    List<UserSubscription> findSubscriptionsInGracePeriod(
            @Param("now") LocalDateTime now,
            @Param("gracePeriodEnd") LocalDateTime gracePeriodEnd
    );

    /**
     * Знайти всі підписки що потребують деактивації
     * Включає:
     * - підписки БЕЗ автопродовження що закінчилися
     * - підписки З автопродовженням що прострочені більше grace period
     *
     * @param now поточний час
     * @param gracePeriodEnd межа grace period (now - N днів)
     * @return список підписок для деактивації
     */
    @Query("SELECT us FROM UserSubscription us " +
            "WHERE us.status = 'ACTIVE' " +
            "AND us.cancelledAt IS NULL " +
            "AND (" +
            "  (us.autoRenew = FALSE AND us.nextBillingDate <= :now) " +
            "  OR " +
            "  (us.autoRenew = TRUE AND us.nextBillingDate <= :gracePeriodEnd)" +
            ")")
    List<UserSubscription> findExpiredSubscriptions(
            @Param("now") LocalDateTime now,
            @Param("gracePeriodEnd") LocalDateTime gracePeriodEnd
    );
}
