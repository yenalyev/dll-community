package platform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import platform.config.security.CustomUserDetailsService;
import platform.config.security.OAuth2SuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ⬇️ CSRF для Java 8
                .csrf()
                .ignoringAntMatchers(
                        "/api/payment/**",              // Всі payment API endpoints
                        "/*/subscription/success/**",    // Return URL від WayForPay
                        "/*/subscription/failed/**"      // Failed URL
                )
                .and()

                .authorizeHttpRequests(authorize -> authorize
                        // --- 1. Публічні сторінки ---
                        .antMatchers(
                                // Статичні ресурси
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                // Шлюзи для входу/реєстрації (без мовного коду)
                                "/login", "/register", "/logout",
                                // Кореневий URL для редіректу
                                "/",
                                // Публічні сторінки з будь-яким мовним кодом
                                "/*/home", "/*/index", "/*/",
                                "/*/login", "/*/register",
                                "/uploads/**",
                                // API для OAuth2
                                "/oauth2/**", "/login/oauth2/**",
                                // ⬇️ ВИПРАВЛЕНО: Success/Failed сторінки для оплати
                                "/*/subscription/success/**",
                                "/*/subscription/failed/**",
                                // API Payment callback (webhook від WayForPay)
                                "/api/payment/callback/**"
                        ).permitAll()

                        // --- 2. Адмін-панель (тільки для ADMIN) ---
                        .antMatchers("/admin/**").hasRole("ADMIN")

                        // --- 3. КАБІНЕТ КОРИСТУВАЧА - ВИМАГАЄ АВТЕНТИФІКАЦІЇ ---
                        .antMatchers("/*/cabinet/**").authenticated()

                        // --- 4. Інші захищені маршрути ---
                        //.antMatchers("/*/lessons/**", "/*/profile/**").authenticated()

                        // --- 5. Всі інші запити - публічні (або authenticated якщо треба) ---
                        .anyRequest().permitAll()
                )

                // --- 4. Налаштування форми входу ---
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/*/login") // Працює з /uk/login, /en/login, /de/login
                        .successHandler((request, response, authentication) -> {
                            String lang = extractLangFromUrl(request);
                            response.sendRedirect("/" + lang + "/");
                        })
                        .failureHandler((request, response, exception) -> {
                            String lang = extractLangFromUrl(request);
                            response.sendRedirect("/" + lang + "/login?error=true");
                        })
                        .usernameParameter("username")
                        .passwordParameter("password")
                )

                // --- 5. Налаштування OAuth2 ---
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2SuccessHandler)
                        .failureUrl("/login?error=oauth")
                )

                // --- 6. Налаштування виходу ---
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            String lang = extractLangFromUrl(request);
                            response.sendRedirect("/" + lang + "/login?logout=true");
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )

                // --- 7. Remember Me ---
                .rememberMe(rememberMe -> rememberMe
                        .key("uniqueAndSecretKey123456")
                        .tokenValiditySeconds(30 * 24 * 60 * 60) // 30 днів
                        .rememberMeParameter("remember-me")
                        .rememberMeCookieName("remember-me")
                        .userDetailsService(customUserDetailsService)
                )

                // --- 8. Обробка помилок доступу ---
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Витягуємо мову з URL
                            String lang = extractLangFromUrl(request);
                            response.sendRedirect("/" + lang + "/login?redirect=" +
                                    request.getRequestURI());
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            String lang = extractLangFromUrl(request);
                            response.sendRedirect("/" + lang + "/access-denied");
                        })
                );

        return http.build();
    }

    /**
     * Витягує мовний код з URL
     */
    private String extractLangFromUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Витягуємо перший сегмент після /
        String[] segments = uri.split("/");
        if (segments.length > 1 && segments[1].matches("[a-z]{2}")) {
            return segments[1]; // uk, en, de тощо
        }
        return "uk"; // За замовчуванням українська
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}