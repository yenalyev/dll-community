package admin.service;

import admin.dto.DashboardStatsDTO;
import admin.dto.RecentOrderDTO;
import admin.dto.RecentUserDTO;
import entity.order.Order;
import entity.user.User;
import entity.user.UserEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.LessonRepository;
import repository.OrderRepository;
import repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервіс для отримання даних дашборду адмін-панелі
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final LessonRepository lessonRepository;

    /**
     * Отримати статистику для дашборду
     */
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        log.debug("Calculating dashboard statistics");

        // Визначаємо період для порівняння (30 днів тому)
        LocalDateTime periodAgo = LocalDateTime.now().minusDays(30);

        // Users статистика
        Long totalUsers = userRepository.count();
        Long usersLastPeriod = userRepository.countUsersSince(periodAgo);
        Double usersGrowth = calculateGrowthPercent(totalUsers - usersLastPeriod, totalUsers);

        // Lessons статистика
        Long totalLessons = lessonRepository.count();
        Long lessonsLastPeriod = lessonRepository.countLessonsSince(periodAgo);
        Double lessonsGrowth = calculateGrowthPercent(totalLessons - lessonsLastPeriod, totalLessons);

        // Orders статистика
        Long totalOrders = orderRepository.count();
        Long ordersLastPeriod = orderRepository.countOrdersSince(periodAgo);
        Double ordersGrowth = calculateGrowthPercent(totalOrders - ordersLastPeriod, totalOrders);

        // Revenue статистика
        Long totalRevenue = orderRepository.calculateTotalRevenue();
        Long revenueLastPeriod = orderRepository.calculateRevenueSince(periodAgo);
        Double revenueGrowth = calculateGrowthPercent(revenueLastPeriod, totalRevenue);

        return DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .usersGrowthPercent(usersGrowth)
                .totalLessons(totalLessons)
                .lessonsGrowthPercent(lessonsGrowth)
                .totalOrders(totalOrders)
                .ordersGrowthPercent(ordersGrowth)
                .totalRevenue(totalRevenue)
                .revenueGrowthPercent(revenueGrowth)
                .build();
    }

    /**
     * Отримати останні замовлення
     */
    @Transactional(readOnly = true)
    public List<RecentOrderDTO> getRecentOrders(int limit) {
        log.debug("Fetching {} recent orders", limit);

        List<Order> orders = orderRepository.findRecentOrders(PageRequest.of(0, limit));

        return orders.stream()
                .map(this::mapToRecentOrderDTO)
                .collect(Collectors.toList());
    }

    /**
     * Отримати останніх користувачів
     */
    @Transactional(readOnly = true)
    public List<RecentUserDTO> getRecentUsers(int limit) {
        log.debug("Fetching {} recent users", limit);

        List<User> users = userRepository.findRecentUsers(PageRequest.of(0, limit));

        return users.stream()
                .map(this::mapToRecentUserDTO)
                .collect(Collectors.toList());
    }

    /**
     * Розрахунок відсотка зростання
     */
    private Double calculateGrowthPercent(Long newCount, Long totalCount) {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }

        Long oldCount = totalCount - (newCount != null ? newCount : 0);
        if (oldCount == 0) {
            return 100.0;
        }

        return ((newCount != null ? newCount : 0) * 100.0) / oldCount;
    }

    /**
     * Мапінг Order -> RecentOrderDTO
     */
    private RecentOrderDTO mapToRecentOrderDTO(Order order) {
        return RecentOrderDTO.builder()
                .orderId(order.getId())
                .orderNumber(String.valueOf(order.getId())) // ← ВИПРАВЛЕНО: використовуємо ID
                .customerName(order.getUser() != null ? order.getUser().getName() : "Невідомий")
                .customerEmail(order.getUser() != null ? getPrimaryEmail(order.getUser()) : "")
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    /**
     * Мапінг User -> RecentUserDTO
     */
    private RecentUserDTO mapToRecentUserDTO(User user) {
        return RecentUserDTO.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(getPrimaryEmail(user))
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Отримати primary email користувача
     */
    private String getPrimaryEmail(User user) {
        if (user == null || user.getEmails() == null) {
            return "";
        }

        return user.getEmails().stream()
                .filter(UserEmail::getIsPrimary)
                .map(UserEmail::getEmail)
                .findFirst()
                .orElse(user.getEmails().isEmpty() ? "" : user.getPrimaryEmail());
    }
}