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

    /**
     * Отримати основний email
     */
    @Transient
    public String getPrimaryEmail() {
        if (emails == null || emails.isEmpty()) {
            return null;
        }
        return emails.stream()
                .filter(UserEmail::getIsPrimary)
                .map(UserEmail::getEmail)
                .findFirst()
                .orElse(emails.iterator().next().getEmail()); // fallback на перший email
    }

    /**
     * Перевірити чи користувач через OAuth2
     */
    @Transient
    public boolean isOAuthUser() {
        return socialAccounts != null && !socialAccounts.isEmpty();
    }

    /**
     * Отримати провайдера OAuth2 (для відображення)
     */
    @Transient
    public String getAuthProvider() {
        if (socialAccounts == null || socialAccounts.isEmpty()) {
            return "LOCAL";
        }
        return socialAccounts.iterator().next().getProvider(); // GOOGLE, FACEBOOK тощо
    }

    /**
     * Отримати мову інтерфейсу
     */
    @Transient
    public String getInterfaceLanguage() {
        if (settings == null || settings.getInterfaceLanguage() == null) {
            return "uk"; // за замовчуванням
        }
        return settings.getInterfaceLanguage();
    }

    /**
     * Чи хоче користувач отримувати розсилку
     */
    @Transient
    public boolean wantsNewsletter() {
        return settings != null && Boolean.TRUE.equals(settings.getWantsNewsletter());
    }
}
