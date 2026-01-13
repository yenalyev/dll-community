package service.user;

import dto.RegisterDto;
import entity.user.*;
import exception.ResourceNotFoundException;
import exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.UserEmailRepository;
import repository.UserRepository;
import repository.UserRoleRepository;

import java.time.LocalDateTime;
import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserEmailRepository userEmailRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Реєстрація нового користувача через email/password
     */
    @Transactional
    public User registerUser(RegisterDto registerDto) {
        // Перевіряємо чи email вже існує
        if (userEmailRepository.existsByEmail(registerDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        // Отримуємо роль USER
        UserRole userRole = userRoleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role USER not found"));

        // Створюємо користувача
        User user = User.builder()
                .name(registerDto.getName())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .role(userRole)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .emails(new HashSet<>())
                .build();

        // Створюємо email
        UserEmail userEmail = UserEmail.builder()
                .user(user)
                .email(registerDto.getEmail())
                .isPrimary(true)
                .isVerified(false)
                .build();

        user.getEmails().add(userEmail);

        // Створюємо налаштування за замовчуванням
        UserSettings settings = UserSettings.builder()
                .user(user)
                .interfaceLanguage("ua")
                .wantsNewsletter(true)
                .theme("LIGHT")
                .build();

        user.setSettings(settings);

        // Зберігаємо
        User savedUser = userRepository.save(user);

        log.info("New user registered: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * Обробка OAuth2 логіна (Google, Facebook)
     */
    @Transactional
    public User processOAuthLogin(String provider, String providerUserId,
                                  String email, String name) {
        // Спробуємо знайти за соціальним акаунтом
        return userRepository.findBySocialAccount(provider, providerUserId)
                .orElseGet(() -> {
                    // Якщо не знайшли, спробуємо знайти за email
                    return userRepository.findByEmail(email)
                            .map(existingUser -> {
                                // Користувач існує, просто додаємо соціальний акаунт
                                linkSocialAccount(existingUser, provider, providerUserId);
                                return existingUser;
                            })
                            .orElseGet(() -> {
                                // Створюємо нового користувача
                                return createUserFromOAuth(provider, providerUserId, email, name);
                            });
                });
    }

    private User createUserFromOAuth(String provider, String providerUserId,
                                     String email, String name) {
        UserRole userRole = userRoleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role USER not found"));

        User user = User.builder()
                .name(name)
                .password(null) // Без пароля для OAuth
                .role(userRole)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .emails(new HashSet<>())
                .socialAccounts(new HashSet<>())
                .build();

        // Email
        UserEmail userEmail = UserEmail.builder()
                .user(user)
                .email(email)
                .isPrimary(true)
                .isVerified(true) // OAuth emails вважаються верифікованими
                .build();

        user.getEmails().add(userEmail);

        // Соціальний акаунт
        UserSocialAccount socialAccount = UserSocialAccount.builder()
                .user(user)
                .provider(provider.toUpperCase())
                .providerUserId(providerUserId)
                .build();

        user.getSocialAccounts().add(socialAccount);

        // Налаштування
        UserSettings settings = UserSettings.builder()
                .user(user)
                .interfaceLanguage("ua")
                .wantsNewsletter(true)
                .theme("LIGHT")
                .build();

        user.setSettings(settings);

        User savedUser = userRepository.save(user);

        log.info("New OAuth user created: provider={}, userId={}", provider, savedUser.getId());

        return savedUser;
    }

    private void linkSocialAccount(User user, String provider, String providerUserId) {
        UserSocialAccount socialAccount = UserSocialAccount.builder()
                .user(user)
                .provider(provider.toUpperCase())
                .providerUserId(providerUserId)
                .build();

        user.getSocialAccounts().add(socialAccount);
        userRepository.save(user);

        log.info("Linked social account: userId={}, provider={}", user.getId(), provider);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    /**
     * Зберегти або оновити користувача
     */
    @Transactional
    public User save(User user) {
        log.debug("Saving user: {}", user.getId() != null ? user.getId() : "new user");
        User savedUser = userRepository.save(user);
        log.info("User saved successfully: {}", savedUser.getId());
        return savedUser;
    }

}
