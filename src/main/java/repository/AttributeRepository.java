package repository;

import entity.attributes.Attribute;
import entity.attributes.AttributeOption;
import entity.attributes.OptionTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {

    // ===== КРОК 1: Завантаження атрибута з його перекладами =====
    @Query("SELECT DISTINCT a FROM Attribute a " +
            "LEFT JOIN FETCH a.translations " +
            "WHERE a.id = :id")
    Optional<Attribute> findByIdWithTranslations(@Param("id") Long id);

    // ===== КРОК 2: Завантаження атрибута з опціями (БЕЗ перекладів опцій) =====
    @Query("SELECT DISTINCT a FROM Attribute a " +
            "LEFT JOIN FETCH a.options " +
            "WHERE a.id = :id")
    Optional<Attribute> findByIdWithOptions(@Param("id") Long id);

    // ===== КРОК 3А: Завантаження самих опцій для атрибута =====
    // Цей запит просто повертає опції без перекладів
    @Query("SELECT DISTINCT o FROM AttributeOption o " +
            "WHERE o.attribute.id = :attributeId")
    List<AttributeOption> findOptionsByAttributeId(@Param("attributeId") Long attributeId);

    // ===== КРОК 3Б: Завантаження перекладів для конкретної опції =====
    // Використовується для кожної опції окремо
    @Query("SELECT DISTINCT o FROM AttributeOption o " +
            "LEFT JOIN FETCH o.translations " +
            "WHERE o.id = :optionId")
    Optional<AttributeOption> findOptionByIdWithTranslations(@Param("optionId") Long optionId);

    // ===== Альтернатива для КРОКУ 3: Завантаження всіх перекладів опцій одним запитом =====
    // Цей запит завантажує всі переклади для всіх опцій атрибута
    @Query("SELECT DISTINCT t FROM OptionTranslation t " +
            "WHERE t.option.attribute.id = :attributeId")
    List<OptionTranslation> findAllOptionTranslationsByAttributeId(@Param("attributeId") Long attributeId);

    // ===== Для списку всіх атрибутів =====
    @Query("SELECT DISTINCT a FROM Attribute a " +
            "LEFT JOIN FETCH a.translations")
    List<Attribute> findAllWithTranslations();

    // ===== Інші методи =====
    boolean existsByName(String name);
}
