package platform.config.security;

import entity.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import service.user.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handler для обробки успішної OAuth2 автентифікації.
 *
 * Використовує @Lazy для розриву циклічної залежності між:
 * SecurityConfig -> OAuth2SuccessHandler -> UserService -> PasswordEncoder -> SecurityConfig
 *
 * Це безпечно, оскільки:
 * 1. Handler викликається тільки після повної ініціалізації Spring Context
 * 2. UserService не використовується при старті додатку
 * 3. Перше звернення відбувається при OAuth логіні, коли всі біни готові
 */
@Slf4j
@Component
public class OAuth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserService userService;

    /**
     * @Lazy розриває циклічну залежність.
     * UserService буде ініціалізований при першому використанні,
     * що гарантовано відбудеться ПІСЛЯ завершення ініціалізації Spring Context.
     */
    public OAuth2SuccessHandler(@Lazy UserService userService) {
        this.userService = userService;
        log.debug("OAuth2SuccessHandler initialized with lazy UserService injection");
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
            // На цьому етапі UserService гарантовано ініціалізований
            User user = userService.processOAuthLogin(provider, providerUserId, email, name);
            log.info("OAuth2 user processed: userId={}, email={}", user.getId(), email);

        } catch (Exception e) {
            log.error("Error processing OAuth2 login for email={}", email, e);
            // Можна перенаправити на сторінку помилки
            response.sendRedirect("/login?error=oauth");
            return;
        }

        // Перенаправляємо користувача
        setDefaultTargetUrl("/");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
