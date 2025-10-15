package entity.lesson;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "lesson_material")
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class LessonMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false, length = 20)
    private MaterialType materialType;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content; // URL або текст

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public enum MaterialType {
        PDF, LINK, IMAGE, TEXT_BLOCK
    }
}
