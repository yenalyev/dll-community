package repository;

import entity.order.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findActiveByUserId(Long userId);

    boolean existsActiveByUserId(Long userId);

    @Query("SELECT us FROM UserSubscription us WHERE us.status = 'ACTIVE' AND us.endDate <= :now")
    List<UserSubscription> findExpiredSubscriptions(@Param("now") LocalDateTime now);


    List<UserSubscription> findByUserIdOrderByCreatedAtDesc(Long userId);
}
