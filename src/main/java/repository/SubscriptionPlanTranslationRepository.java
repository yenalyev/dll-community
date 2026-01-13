package repository;

import entity.order.SubscriptionPlanTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionPlanTranslationRepository extends JpaRepository<SubscriptionPlanTranslation, Long> {

    /**
     * Знайти переклад плану для мови
     */
    Optional<SubscriptionPlanTranslation> findByPlanIdAndLang(Long planId, String lang);
}
