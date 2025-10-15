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
     * Він буде аналізувати URL на наявність мовного коду (/uk/, /en/ і т.д.).
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Створюємо екземпляр нашого перехоплювача, який тепер
        // є окремим, повноцінним класом.
        registry.addInterceptor(new PathLocaleInterceptor());
    }
}
