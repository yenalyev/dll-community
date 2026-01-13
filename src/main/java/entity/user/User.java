package entity.user;

import entity.enums.SubscriptionStatus;
import entity.order.UserSubscription;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserEmail> emails;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserSocialAccount> socialAccounts;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserSettings settings;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserSubscription> subscriptions = new HashSet<>();

    // Допоміжний метод для отримання активної підписки
    @Transient
    public UserSubscription getActiveSubscription() {
        if (subscriptions == null) return null;
        return subscriptions.stream()
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .filter(sub -> LocalDateTime.now().isBefore(sub.getEndDate()))
                .findFirst()
                .orElse(null);
    }

    @Transient
    public boolean hasActiveSubscription() {
        return getActiveSubscription() != null;
    }
}
