package entity.user;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "user_social_account")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class UserSocialAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String provider; // GOOGLE, FACEBOOK

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;
}
