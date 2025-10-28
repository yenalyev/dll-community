package repository;

import entity.lesson.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * Завантажити урок з перекладами
     */
    @Query("SELECT DISTINCT l FROM Lesson l " +
            "LEFT JOIN FETCH l.translations " +
            "WHERE l.id = :id")
    Optional<Lesson> findByIdWithTranslations(@Param("id") Long id);

    /**
     * Завантажити урок з цінами
     */
    @Query("SELECT DISTINCT l FROM Lesson l " +
            "LEFT JOIN FETCH l.prices " +
            "WHERE l.id = :id")
    Optional<Lesson> findByIdWithPrices(@Param("id") Long id);

    /**
     * Завантажити урок з матеріалами
     */
    @Query("SELECT DISTINCT l FROM Lesson l " +
            "LEFT JOIN FETCH l.materials " +
            "WHERE l.id = :id")
    Optional<Lesson> findByIdWithMaterials(@Param("id") Long id);

    /**
     * Завантажити урок з атрибутами
     */
    @Query("SELECT DISTINCT l FROM Lesson l " +
            "LEFT JOIN FETCH l.attributes " +
            "WHERE l.id = :id")
    Optional<Lesson> findByIdWithAttributes(@Param("id") Long id);

    /**
     * Завантажити всі уроки з перекладами (для списку)
     */
    @Query("SELECT DISTINCT l FROM Lesson l " +
            "LEFT JOIN FETCH l.translations")
    List<Lesson> findAllWithTranslations();

    /**
     * Перевірка унікальності slug для певної мови
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM LessonTranslation t " +
            "WHERE t.lang = :lang AND t.slug = :slug AND t.lesson.id != :lessonId")
    boolean existsByLangAndSlugExcludingLesson(
            @Param("lang") String lang,
            @Param("slug") String slug,
            @Param("lessonId") Long lessonId
    );

    /**
     * Перевірка унікальності slug для певної мови (для нового уроку)
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM LessonTranslation t " +
            "WHERE t.lang = :lang AND t.slug = :slug")
    boolean existsByLangAndSlug(@Param("lang") String lang, @Param("slug") String slug);


    /**
     * Завантажити всі уроки з перекладами, відсортовані за датою створення (найновіші спочатку)
     */
    @Query("SELECT DISTINCT l FROM Lesson l " +
            "LEFT JOIN FETCH l.translations " +
            "ORDER BY l.createdAt DESC")
    List<Lesson> findAllWithTranslationsOrderByCreatedAtDesc();

    /**
     * Завантажити перші N уроків з перекладами, відсортовані за датою створення (найновіші спочатку)
     * Примітка: JPQL не підтримує LIMIT, тому цей метод буде працювати через назву
     */
    @Query("SELECT DISTINCT l FROM Lesson l " +
            "LEFT JOIN FETCH l.translations " +
            "ORDER BY l.createdAt DESC")
    List<Lesson> findTopLessonsWithTranslations();

    /**
     * Знайти урок за slug та мовою
     */
    @Query("SELECT t.lesson FROM LessonTranslation t " +
            "WHERE t.slug = :slug AND t.lang = :lang")
    Optional<Lesson> findBySlugAndLang(@Param("slug") String slug, @Param("lang") String lang);

}
