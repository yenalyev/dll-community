package entity.user;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "user_settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class UserSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "interface_language", length = 2, nullable = false)
    private String interfaceLanguage = "ua";

    @Column(name = "wants_newsletter", nullable = false)
    private Boolean wantsNewsletter = true;

    @Column(length = 20)
    private String theme = "LIGHT"; // LIGHT, DARK
}
