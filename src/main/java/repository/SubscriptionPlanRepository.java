package repository;


import entity.order.SubscriptionPlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    @EntityGraph(attributePaths = {"translations", "prices"})
    List<SubscriptionPlan> findByIsActiveTrue();

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
