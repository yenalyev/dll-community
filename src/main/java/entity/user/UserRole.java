package entity.user;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "user_role")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // USER, MANAGER, ADMIN

    @Column(length = 255)
    private String description;
}
