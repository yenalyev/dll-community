package repository;

import entity.user.UserEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserEmailRepository extends JpaRepository<UserEmail, Long> {
    Optional<UserEmail> findByEmail(String email);
    boolean existsByEmail(String email);
}
