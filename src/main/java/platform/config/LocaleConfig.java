package platform.config;

import entity.enums.Language;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class LocaleConfig implements WebMvcConfigurer {

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
        registry.addInterceptor(new PathLocaleInterceptor())
                // Виключаємо шляхи, які не повинні оброблятися
                // перехоплювачем мови
                .excludePathPatterns(
                        "/admin/**",      // Вся адмін-панель
                        "/css/**",        // Статичні ресурси
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/error"          // Сторінка помилок Spring
                );
    }
}
