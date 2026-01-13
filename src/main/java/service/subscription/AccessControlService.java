package service.subscription;

import entity.lesson.Lesson;
import entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessControlService {

    private final SubscriptionService subscriptionService;

    /**
     * Перевірити чи має користувач доступ до уроку
     */
    public boolean hasAccessToLesson(User user, Lesson lesson) {
        // Публічні уроки доступні всім
        if (lesson.getAccessLevel() == Lesson.AccessLevel.FREE) {
            return true;
        }

        // Адміни та менеджери мають доступ до всього
        if (isAdminOrManager(user)) {
            return true;
        }

        // Для платних уроків перевіряємо підписку
        return subscriptionService.hasActiveSubscription(user.getId());
    }

    /**
     * Перевірити чи може користувач завантажити матеріали
     */
    public boolean canDownloadMaterials(User user, Lesson lesson) {
        return hasAccessToLesson(user, lesson);
    }

    /**
     * Отримати повідомлення про необхідність підписки
     */
    public String getAccessDeniedMessage(String lang) {
        switch (lang) {
            case "uk": return "Для доступу до цього уроку потрібна активна підписка";
            case "de": return "Für den Zugriff auf diese Lektion ist ein aktives Abonnement erforderlich";
            default: return "Active subscription required to access this lesson";
        }
    }

    private boolean isAdminOrManager(User user) {
        String roleName = user.getRole().getName();
        return "ADMIN".equals(roleName) || "MANAGER".equals(roleName);
    }
}
