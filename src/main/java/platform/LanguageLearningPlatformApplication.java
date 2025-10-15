package platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Головний клас для запуску Spring Boot додатку.
 * ComponentScan сканує всі пакети проекту для пошуку Spring компонентів.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "platform",      // Головний пакет з конфігураціями
        "controller",    // Контролери
        "service",       // Сервіси
        "repository",    // Репозиторії
        "dto",           // DTO
        "exception"      // Винятки
})
@EntityScan(basePackages = "entity")  // Сканування JPA сутностей
@EnableJpaRepositories(basePackages = "repository")  // Репозиторії JPA
public class LanguageLearningPlatformApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(LanguageLearningPlatformApplication.class, args);
    }
}
