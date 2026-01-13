package repository;

import entity.order.SubscriptionPlanPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Currency;
import java.util.Optional;

@Repository
public interface SubscriptionPlanPriceRepository extends JpaRepository<SubscriptionPlanPrice, Long> {

    /**
     * Знайти ціну плану в певній валюті
     */
    Optional<SubscriptionPlanPrice> findByPlanIdAndCurrency(Long planId, Currency currency);
}
