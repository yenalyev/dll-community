package repository;


import entity.order.SubscriptionPlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    /**
     * Знайти активні плани підписки з завантаженням translations і prices
     */
    @Query("SELECT DISTINCT sp FROM SubscriptionPlan sp " +
            "LEFT JOIN FETCH sp.prices " +
            "WHERE sp.isActive = true " +
            "ORDER BY sp.sortOrder")
    List<SubscriptionPlan> findByIsActiveTrue();

    /**
     * Дозавантажити translations для планів
     */
    @Query("SELECT DISTINCT sp FROM SubscriptionPlan sp " +
            "LEFT JOIN FETCH sp.translations " +
            "WHERE sp IN :plans")
    List<SubscriptionPlan> loadTranslations(@Param("plans") List<SubscriptionPlan> plans);

    // Отримати план з перекладами
    @Query("SELECT DISTINCT p FROM SubscriptionPlan p " +
            "LEFT JOIN FETCH p.translations " +
            "WHERE p.id = :id")
    Optional<SubscriptionPlan> findByIdWithTranslations(@Param("id") Long id);

    // Отримати план з цінами
    @Query("SELECT DISTINCT p FROM SubscriptionPlan p " +
            "LEFT JOIN FETCH p.prices " +
            "WHERE p.id = :id")
    Optional<SubscriptionPlan> findByIdWithPrices(@Param("id") Long id);

    // Отримати всі плани (без eager loading колекцій)
    @Query("SELECT p FROM SubscriptionPlan p ORDER BY p.durationInDays")
    List<SubscriptionPlan> findAllOrdered();


}
