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
@RequiredArgsConstructor  // <-- ДОДАТИ ЦЮ АНОТАЦІЮ для DI
public class LocaleConfig implements WebMvcConfigurer {

    // ===== ДОДАТИ ЦЕ ПОЛЕ =====
    private final SubscriptionStatusInterceptor subscriptionStatusInterceptor;
    // =========================

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
     * Реєструємо наш кастомний перехоплювач в Spring MVC.
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
                .order(0);  // <-- Виконується ПЕРШИМ

        // ===== 2. SUBSCRIPTION STATUS INTERCEPTOR (НОВИЙ) =====
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
                .order(1);  // <-- Виконується ДРУГИМ
    }
}