package entity.lesson;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "lesson_price")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class LessonPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false)
    private Long amount; // в копійках/центах

    public enum Currency {
        UAH, EUR, USD
    }
}
