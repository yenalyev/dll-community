package platform.config;

import entity.enums.Language;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
@RequiredArgsConstructor
public class LocaleConfig implements WebMvcConfigurer {

    private final SubscriptionStatusInterceptor subscriptionStatusInterceptor;
    private final RedirectInterceptor redirectInterceptor;  // ← ДОДАНО

    /**
     * LocaleResolver визначає, яка мова використовується.
     * SessionLocaleResolver зберігає вибір мови в сесії користувача.
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        // Встановлюємо мову за замовчуванням, використовуючи наш enum
        localeResolver.setDefaultLocale(new Locale(Language.UK.getCode()));
        return localeResolver;
    }

    /**
     * Реєструємо наші кастомні перехоплювачі в Spring MVC.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // ===== 1. LOCALE INTERCEPTOR (існуючий) =====
        registry.addInterceptor(new PathLocaleInterceptor())
                .excludePathPatterns(
                        "/admin/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/error"
                )
                .order(0);  // Виконується ПЕРШИМ

        // ===== 2. SUBSCRIPTION STATUS INTERCEPTOR (існуючий) =====
        registry.addInterceptor(subscriptionStatusInterceptor)
                .addPathPatterns("/**")  // Всі шляхи
                .excludePathPatterns(
                        "/css/**",        // Статичні ресурси
                        "/js/**",
                        "/images/**",
                        "/uploads/**",    // Завантажені файли
                        "/webjars/**",
                        "/api/**",        // REST API
                        "/error"          // Сторінка помилок
                )
                .order(1);  // Виконується ДРУГИМ

        // ===== 3. REDIRECT INTERCEPTOR (НОВИЙ) =====
        registry.addInterceptor(redirectInterceptor)
                .addPathPatterns(
                        "/*/login",      // Зберігати redirect на сторінці логіна
                        "/*/register"    // Зберігати redirect на сторінці реєстрації
                )
                .order(2);  // Виконується ТРЕТІМ
    }
}