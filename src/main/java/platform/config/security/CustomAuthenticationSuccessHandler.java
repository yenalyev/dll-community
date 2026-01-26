package platform.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import platform.config.RedirectInterceptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handler для обробки успішної форм-автентифікації (email/password)
 *
 * Після успішного логіна перевіряє чи є збережений redirect URL в session.
 * Якщо є - редірект туди, якщо немає - на головну сторінку.
 */
@Slf4j
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        log.info("Form login successful for user: {}", authentication.getName());

        // Отримуємо мову з URL
        String lang = extractLangFromUrl(request);

        // Перевіряємо чи є збережений redirect URL
        HttpSession session = request.getSession(false);
        String redirectUrl = RedirectInterceptor.getRedirectUrl(session);

        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            // Очищаємо redirect URL з session
            RedirectInterceptor.clearRedirectUrl(session);

            log.info("Redirecting user {} to saved URL: {}",
                    authentication.getName(), redirectUrl);

            response.sendRedirect(redirectUrl);
        } else {
            // Редірект на головну сторінку
            String defaultUrl = "/" + lang + "/";

            log.debug("No redirect URL found, redirecting to: {}", defaultUrl);

            response.sendRedirect(defaultUrl);
        }
    }

    /**
     * Витягує мовний код з URL
     */
    private String extractLangFromUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String[] segments = uri.split("/");

        if (segments.length > 1 && segments[1].matches("[a-z]{2}")) {
            return segments[1]; // uk, en, de
        }

        return "uk"; // За замовчуванням українська
    }
}
