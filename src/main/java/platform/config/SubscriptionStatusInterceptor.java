package platform.config;

import entity.order.UserSubscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import platform.config.security.CustomUserDetails;
import service.subscription.SubscriptionService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor для автоматичного додавання інформації про підписку в Model.
 *
 * Додає в кожен view наступні змінні:
 * - hasActiveSubscription (boolean) - чи має користувач активну підписку
 * - activeSubscription (UserSubscription) - об'єкт активної підписки (якщо є)
 *
 * Використовується в header для показу статусу підписки.
 *
 * @author DLL Community
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionStatusInterceptor implements HandlerInterceptor {

    private final SubscriptionService subscriptionService;

    /**
     * Викликається ПІСЛЯ виконання контролера, але ПЕРЕД рендерингом view.
     * Тут ми додаємо дані про підписку в Model.
     */
    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView
    ) {
        // Якщо немає ModelAndView (наприклад, REST endpoint або redirect) - пропускаємо
        if (modelAndView == null) {
            log.trace("Skipping subscription status check - no ModelAndView");
            return;
        }

        // Якщо це redirect - пропускаємо
        String viewName = modelAndView.getViewName();
        if (viewName != null && viewName.startsWith("redirect:")) {
            log.trace("Skipping subscription status check - redirect detected");
            return;
        }

        try {
            // Отримати поточну автентифікацію
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // Перевірити чи користувач автентифікований
            if (auth == null || !auth.isAuthenticated()) {
                log.trace("User not authenticated, setting hasActiveSubscription=false");
                addNoSubscriptionToModel(modelAndView);
                return;
            }

            // Перевірити чи це не AnonymousUser
            if (auth.getPrincipal() == null || "anonymousUser".equals(auth.getPrincipal())) {
                log.trace("Anonymous user detected, setting hasActiveSubscription=false");
                addNoSubscriptionToModel(modelAndView);
                return;
            }

            // Перевірити чи це наш CustomUserDetails
            if (!(auth.getPrincipal() instanceof CustomUserDetails)) {
                log.debug("Unknown principal type: {}, setting hasActiveSubscription=false",
                        auth.getPrincipal().getClass().getName());
                addNoSubscriptionToModel(modelAndView);
                return;
            }

            // Отримати CustomUserDetails
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getId();

            // Перевірити чи існує активна підписка
            UserSubscription activeSubscription = subscriptionService.getActiveSubscription(userId);
            boolean hasActiveSubscription = activeSubscription != null;

            // Додати в Model
            modelAndView.addObject("hasActiveSubscription", hasActiveSubscription);
            modelAndView.addObject("activeSubscription", activeSubscription);

            log.debug("Added subscription status to model for user {}: hasActiveSubscription={}",
                    userId, hasActiveSubscription);

            // Якщо є активна підписка - додаємо інфо про дні до закінчення
            if (activeSubscription != null) {
                long daysUntilExpiration = java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDateTime.now(),
                        activeSubscription.getEndDate()
                );
                modelAndView.addObject("daysUntilExpiration", daysUntilExpiration);

                log.debug("Subscription expires in {} days", daysUntilExpiration);
            }

        } catch (Exception e) {
            log.error("Error checking subscription status in interceptor", e);
            // У випадку помилки - безпечно встановити що підписки немає
            addNoSubscriptionToModel(modelAndView);
        }
    }

    /**
     * Helper метод для додавання "немає підписки" в Model
     */
    private void addNoSubscriptionToModel(ModelAndView modelAndView) {
        modelAndView.addObject("hasActiveSubscription", false);
        modelAndView.addObject("activeSubscription", null);
        modelAndView.addObject("daysUntilExpiration", 0);
    }
}
