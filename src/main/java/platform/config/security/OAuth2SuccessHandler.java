package platform.config.security;

import entity.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import platform.config.RedirectInterceptor;
import service.user.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handler для обробки успішної OAuth2 автентифікації.
 *
 * Після успішного OAuth2 логіну:
 * 1. Замінює DefaultOAuth2User на CustomUserDetails
 * 2. Перевіряє чи є збережений redirect URL в session
 * 3. Редірект на збережений URL або на головну
 */
@Slf4j
@Component
public class OAuth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;

    public OAuth2SuccessHandler(
            @Lazy UserService userService,
            @Lazy CustomUserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        log.debug("OAuth2SuccessHandler initialized with lazy injection");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            log.warn("Unexpected authentication type: {}", authentication.getClass().getName());
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String providerUserId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        log.info("OAuth2 authentication successful: provider={}, email={}", provider, email);

        try {
            // Обробляємо OAuth користувача (створюємо або оновлюємо в БД)
            User user = userService.processOAuthLogin(provider, providerUserId, email, name);
            log.info("OAuth2 user processed: userId={}, email={}", user.getId(), email);

            // ВАЖЛИВО: Завантажуємо CustomUserDetails для цього користувача
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);

            // Створюємо новий Authentication token з CustomUserDetails
            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            newAuth.setDetails(authentication.getDetails());

            // КРИТИЧНО: Оновлюємо SecurityContext з новим Authentication
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            log.debug("Updated SecurityContext with CustomUserDetails for user: {}", email);

        } catch (Exception e) {
            log.error("Error processing OAuth2 login for email={}", email, e);
            response.sendRedirect("/login?error=oauth");
            return;
        }

        // ========== REDIRECT LOGIC ==========
        // Перевіряємо чи є збережений redirect URL в session
        HttpSession session = request.getSession(false);
        String redirectUrl = RedirectInterceptor.getRedirectUrl(session);

        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            // Очищаємо redirect URL з session
            RedirectInterceptor.clearRedirectUrl(session);

            log.info("OAuth2: Redirecting user {} to saved URL: {}",
                    email, redirectUrl);

            response.sendRedirect(redirectUrl);
            return;
        }
        // ====================================

        // Редірект на головну сторінку якщо немає збереженого URL
        setDefaultTargetUrl("/");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}