package entity.lesson;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "lesson_translation", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"lang", "slug"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class LessonTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, length = 2)
    private String lang; // ua, en, de

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "meta_description", length = 160)
    private String metaDescription;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;
}
