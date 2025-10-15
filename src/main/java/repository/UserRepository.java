package repository;

import entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u " +
            "JOIN FETCH u.emails e " +
            "JOIN FETCH u.role " +
            "WHERE e.email = :email AND e.isPrimary = true")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u " +
            "JOIN FETCH u.socialAccounts sa " +
            "WHERE sa.provider = :provider AND sa.providerUserId = :providerUserId")
    Optional<User> findBySocialAccount(
            @Param("provider") String provider,
            @Param("providerUserId") String providerUserId
    );

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM UserEmail e WHERE e.email = :email")
    boolean existsByEmail(@Param("email") String email);
}
