package admin.service;

import admin.dto.AdminUserDTO;
import admin.dto.UserSettingsDTO;
import admin.dto.UserStatisticsDTO;
import admin.dto.UserSubscriptionDTO;
import entity.enums.SubscriptionStatus;
import entity.order.Order;
import entity.user.User;
import entity.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.OrderRepository;
import repository.UserRepository;
import repository.UserRoleRepository;
import repository.UserSubscriptionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * Отримати всіх користувачів з пагінацією
     */
    public Page<AdminUserDTO> getAllUsers(int page, int size) {
        log.debug("Getting all users: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<User> usersPage = userRepository.findAllWithDetails(pageable);

        return usersPage.map(this::convertToDTO);
    }

    /**
     * Отримати користувачів з фільтрами
     */
    public Page<AdminUserDTO> getUsersWithFilters(
            String email,
            String name,
            String role,
            Boolean isActive,
            Boolean hasSubscription,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {
        log.debug("Getting users with filters: email={}, name={}, role={}, active={}, hasSub={}",
                email, name, role, isActive, hasSubscription);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        Page<User> usersPage;

        // Пріоритизуємо фільтри
        if (email != null && !email.trim().isEmpty()) {
            usersPage = userRepository.findByEmailContaining(email.trim(), pageable);
        } else if (name != null && !name.trim().isEmpty()) {
            usersPage = userRepository.findByNameContaining(name.trim(), pageable);
        } else if (role != null && !role.isEmpty()) {
            usersPage = userRepository.findByRoleName(role, pageable);
        } else if (isActive != null) {
            usersPage = userRepository.findByIsActive(isActive, pageable);
        } else if (hasSubscription != null) {
            LocalDateTime now = LocalDateTime.now();
            if (hasSubscription) {
                usersPage = userRepository.findUsersWithActiveSubscriptions(now, pageable);
            } else {
                usersPage = userRepository.findUsersWithoutActiveSubscriptions(now, pageable);
            }
        } else if (from != null && to != null) {
            usersPage = userRepository.findByCreatedAtBetween(from, to, pageable);
        } else {
            usersPage = userRepository.findAllWithDetails(pageable);
        }

        return usersPage.map(this::convertToDTO);
    }

    /**
     * Отримати користувача по ID з повними деталями
     */
    public AdminUserDTO getUserById(Long id) {
        log.debug("Getting user by id: {}", id);

        User user = userRepository.findByIdWithAllData(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }

        return convertToDTOWithDetails(user);
    }

    /**
     * Отримати статистику користувачів
     */
    public UserStatisticsDTO getUserStatistics() {
        log.debug("Calculating user statistics");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusWeeks(1);
        LocalDateTime monthStart = now.minusMonths(1);

        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countActiveUsers();
        Long inactiveUsers = totalUsers - activeUsers;

        Long usersWithSubs = userRepository.countUsersWithActiveSubscriptions(now);
        Long usersWithoutSubs = totalUsers - usersWithSubs;

        Long newUsersToday = userRepository.countUsersSince(todayStart);
        Long newUsersThisWeek = userRepository.countUsersSince(weekStart);
        Long newUsersThisMonth = userRepository.countUsersSince(monthStart);

        Long adminUsers = userRepository.countByRoleName("ADMIN");
        Long managerUsers = userRepository.countByRoleName("MANAGER");
        Long regularUsers = userRepository.countByRoleName("USER");

        // Retention rate
        Double retentionRate = totalUsers > 0
                ? (usersWithSubs.doubleValue() / totalUsers.doubleValue()) * 100
                : 0.0;

        // TODO: Додати підрахунок OAuth vs Local users

        return UserStatisticsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .usersWithActiveSubscriptions(usersWithSubs)
                .usersWithoutSubscriptions(usersWithoutSubs)
                .newUsersToday(newUsersToday)
                .newUsersThisWeek(newUsersThisWeek)
                .newUsersThisMonth(newUsersThisMonth)
                .adminUsers(adminUsers)
                .managerUsers(managerUsers)
                .regularUsers(regularUsers)
                .subscriptionRetentionRate(retentionRate)
                .build();
    }

    // ========== МЕТОДИ УПРАВЛІННЯ КОРИСТУВАЧАМИ ==========

    /**
     * Заблокувати/розблокувати користувача
     */
    @Transactional
    public void toggleUserActiveStatus(Long userId) {
        log.info("Toggling active status for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setIsActive(!user.getIsActive());
        userRepository.save(user);

        log.info("User {} is now {}", userId, user.getIsActive() ? "ACTIVE" : "BLOCKED");
    }

    /**
     * Змінити роль користувача
     */
    @Transactional
    public void changeUserRole(Long userId, String newRoleName) {
        log.info("Changing role for user {} to {}", userId, newRoleName);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        UserRole newRole = userRoleRepository.findByName(newRoleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + newRoleName));

        user.setRole(newRole);
        userRepository.save(user);

        log.info("User {} role changed to {}", userId, newRoleName);
    }

    /**
     * Подарувати підписку користувачу
     */
    @Transactional
    public void giftSubscription(Long userId, Long planId) {
        log.info("Gifting subscription to user {}: planId={}", userId, planId);

        // TODO: Реалізувати через SubscriptionService
        // subscriptionService.createSubscription(user, plan, null);

        log.warn("Gift subscription not yet implemented");
    }

    // ========== ПРИВАТНІ МЕТОДИ КОНВЕРТАЦІЇ ==========

    /**
     * Конвертувати User entity в AdminUserDTO (легка версія)
     */
    private AdminUserDTO convertToDTO(User user) {
        // Статистика підписок
        boolean hasActiveSubscription = user.hasActiveSubscription();
        UserSubscriptionDTO activeSubDTO = null;

        if (hasActiveSubscription) {
            activeSubDTO = convertSubscriptionToDTO(user.getActiveSubscription());
        }

        // Статистика замовлень
        List<Order> userOrders = orderRepository.findByUserId(user.getId());

        Integer totalOrders = userOrders.size();
        Integer completedOrders = (int) userOrders.stream()
                .filter(o -> o.getStatus() == entity.enums.OrderStatus.COMPLETED)
                .count();

        Long totalSpent = userOrders.stream()
                .filter(o -> o.getStatus() == entity.enums.OrderStatus.COMPLETED)
                .mapToLong(Order::getTotalAmount)
                .sum();

        Integer purchasedLessons = orderRepository.findPurchasedLessonsByUserId(user.getId()).size();

        LocalDateTime lastOrderDate = userOrders.stream()
                .map(Order::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return AdminUserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .primaryEmail(user.getPrimaryEmail())
                .role(user.getRole().getName())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .isOAuthUser(user.isOAuthUser())
                .authProvider(user.getAuthProvider())
                .hasActiveSubscription(hasActiveSubscription)
                .activeSubscription(activeSubDTO)
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .totalSpent(totalSpent)
                .purchasedLessons(purchasedLessons)
                .lastOrderDate(lastOrderDate)
                .build();
    }

    /**
     * Конвертувати User entity в AdminUserDTO з повними деталями
     */
    private AdminUserDTO convertToDTOWithDetails(User user) {
        AdminUserDTO dto = convertToDTO(user);

        // Додати всі emails
        List<String> allEmails = user.getEmails().stream()
                .map(e -> e.getEmail())
                .collect(Collectors.toList());
        dto.setAllEmails(allEmails);

        // Додати налаштування
        if (user.getSettings() != null) {
            UserSettingsDTO settingsDTO = UserSettingsDTO.builder()
                    .interfaceLanguage(user.getSettings().getInterfaceLanguage())
                    .wantsNewsletter(user.getSettings().getWantsNewsletter())
                    .theme(user.getSettings().getTheme())
                    .build();
            dto.setSettings(settingsDTO);
        }

        // Підрахувати загальну кількість підписок (історія)
        List<entity.order.UserSubscription> allSubscriptions =
                subscriptionRepository.findByUserId(user.getId()).orElse(null);

        if (allSubscriptions != null) {
            dto.setTotalSubscriptions(allSubscriptions.size());
        }

        return dto;
    }

    private UserSubscriptionDTO convertSubscriptionToDTO(entity.order.UserSubscription sub) {
        if (sub == null) return null;

        return UserSubscriptionDTO.builder()
                .id(sub.getId())
                .planName(sub.getSubscriptionPlan().getNameForLang("uk"))
                .durationInDays(sub.getSubscriptionPlan().getDurationInDays())
                .status(sub.getStatus())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .autoRenew(sub.getAutoRenew())
                .cancelledAt(sub.getCancelledAt())
                .build();
    }
}