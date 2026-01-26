package platform.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Interceptor для збереження redirect URL в session
 *
 * Коли користувач переходить на /login або /register з параметром ?redirect=...
 * цей interceptor зберігає URL в session, щоб після логіна/реєстрації
 * повернути користувача на оригінальну сторінку.
 */
@Slf4j
@Component
public class RedirectInterceptor implements HandlerInterceptor {

    public static final String REDIRECT_URL_SESSION_KEY = "REDIRECT_AFTER_LOGIN";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        String redirectParam = request.getParameter("redirect");

        if (redirectParam != null && !redirectParam.isEmpty()) {
            // Валідація redirect URL (безпека!)
            if (isValidRedirectUrl(redirectParam)) {
                HttpSession session = request.getSession();
                session.setAttribute(REDIRECT_URL_SESSION_KEY, redirectParam);

                log.debug("Saved redirect URL to session: {}", redirectParam);
            } else {
                log.warn("Invalid redirect URL attempted: {}", redirectParam);
            }
        }

        return true;
    }

    /**
     * Валідація redirect URL для безпеки
     * Дозволяємо тільки внутрішні URL (починаються з /)
     */
    private boolean isValidRedirectUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        // Дозволяємо тільки відносні URL (починаються з /)
        if (!url.startsWith("/")) {
            return false;
        }

        // Забороняємо подвійні слеші (запобігає open redirect)
        if (url.startsWith("//")) {
            return false;
        }

        // Забороняємо небезпечні протоколи
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains("javascript:") ||
                lowerUrl.contains("data:") ||
                lowerUrl.contains("vbscript:")) {
            return false;
        }

        return true;
    }

    /**
     * Отримати збережений redirect URL з session
     */
    public static String getRedirectUrl(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object redirectUrl = session.getAttribute(REDIRECT_URL_SESSION_KEY);
        return redirectUrl != null ? redirectUrl.toString() : null;
    }

    /**
     * Очистити redirect URL з session після використання
     */
    public static void clearRedirectUrl(HttpSession session) {
        if (session != null) {
            session.removeAttribute(REDIRECT_URL_SESSION_KEY);
            log.debug("Cleared redirect URL from session");
        }
    }
}
