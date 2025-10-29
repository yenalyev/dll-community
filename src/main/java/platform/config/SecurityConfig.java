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
import platform.config.security.CustomUserDetailsService;
import platform.config.security.OAuth2SuccessHandler;

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
                .authorizeHttpRequests(authorize -> authorize
                        // --- 1. Публічні сторінки ---
                        .antMatchers(
                                // Статичні ресурси
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                // Шлюзи для входу/реєстрації (без мовного коду)
                                "/login", "/register",
                                // Кореневий URL для редіректу
                                "/",
                                // Публічні сторінки з будь-яким мовним кодом
                                "/*/home", "/*/index", "/*/",
                                "/*/login", "/*/register",
                                "/uploads/**"
                        ).permitAll()
                        // --- 2. Адмін-панель (тільки для ADMIN, БЕЗ мовного префікса) ---
                        .antMatchers("/admin/**").hasRole("ADMIN")
                        // --- 3. Всі інші запити вимагають автентифікації ---
                        //.anyRequest().authenticated()
                )
                // --- 4. Налаштування форми входу ---
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/{lang}/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
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
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )
                // ✅ ДОДАЄМО REMEMBER ME
                .rememberMe(rememberMe -> rememberMe
                        .key("uniqueAndSecretKey123456")  // Змініть на свій секретний ключ!
                        .tokenValiditySeconds(30 * 24 * 60 * 60) // 30 днів (в секундах)
                        .rememberMeParameter("remember-me") // Ім'я параметра з форми
                        .rememberMeCookieName("remember-me") // Ім'я cookie
                        .userDetailsService(customUserDetailsService) // Потрібно додати
                )
                // --- 7. Налаштування обробки помилок доступу ---
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied") // Сторінка при відмові в доступі
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
