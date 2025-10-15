package platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Клас конфігурації для Spring Security.
 * Визначає правила доступу до ресурсів, налаштування форми входу та інші аспекти безпеки.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // ========================================================================
                        // Ось КЛЮЧОВИЙ РЯДОК!
                        // Ми дозволяємо доступ (permitAll) до головної сторінки ("/")
                        // та до всіх статичних ресурсів (стилі, скрипти, зображення)
                        // для абсолютно всіх користувачів, навіть анонімних.
                        // ========================================================================
                        .antMatchers("/", "/home", "/index", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // Всі інші запити (.anyRequest()) вимагають автентифікації (.authenticated()).
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
}

    /**
     * Бін для шифрування паролів.
     * Використовує надійний алгоритм BCrypt.
     * Цей бін буде автоматично використовуватися Spring Security для перевірки паролів.
     *
     * @return реалізація PasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
