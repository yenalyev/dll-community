package repository;

import entity.attributes.AttributePlacement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttributePlacementRepository extends JpaRepository<AttributePlacement, Integer> {
    Optional<AttributePlacement> findByKey(String key);
    boolean existsByKey(String key);
}
