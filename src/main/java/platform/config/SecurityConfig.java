package platform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import platform.config.security.OAuth2SuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // --- 1. Очищений список публічних сторінок ---
                        .antMatchers(
                                // Статичні ресурси
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                // Шлюзи для входу/реєстрації (без мовного коду)
                                "/login", "/register",
                                // Кореневий URL для редіректу
                                "/",
                                // Публічні сторінки з будь-яким мовним кодом
                                "/*/home", "/*/index", "/*/",
                                "/*/login", "/*/register"
                        ).permitAll()
                        // Всі інші запити вимагають автентифікації
                        .anyRequest().authenticated()
                )
                // --- 2. Налаштування форми входу ---
                .formLogin(formLogin -> formLogin
                        // Сторінка входу, яку бачить Spring Security (наш шлюз)
                        .loginPage("/login")
                        // URL, на який відправляються дані форми (POST-запит).
                        // Він повинен мати префікс, оскільки форма знаходиться на сторінці /{lang}/login
                        .loginProcessingUrl("/{lang}/login")
                        .defaultSuccessUrl("/", true) // Редірект на /, який потім перенаправить на /uk/
                        .failureUrl("/login?error=true") // При помилці повертаємо на шлюз
                        .usernameParameter("username")
                        .passwordParameter("password")
                )
                // --- 3. Налаштування OAuth2 ---
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // Користувач починає OAuth2 з нашої сторінки входу
                        .successHandler(oAuth2SuccessHandler)
                        .failureUrl("/login?error=oauth")
                )
                // --- 4. Налаштування виходу ---
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL для виходу залишається простим
                        .logoutSuccessUrl("/login?logout=true") // Після виходу перенаправляємо на шлюз
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
