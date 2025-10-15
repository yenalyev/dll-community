package platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Головний клас для запуску Spring Boot додатку.
 * Важливо, щоб цей клас знаходився у кореневому пакеті (наприклад, com.dllcommunity.platform),
 * щоб Spring міг коректно сканувати всі компоненти, сервіси та репозиторії.
 */
@SpringBootApplication
public class LanguageLearningPlatformApplication implements WebMvcConfigurer {


    public static void main(String[] args) {
        SpringApplication.run(LanguageLearningPlatformApplication.class, args);
    }

}
