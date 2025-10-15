package controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Цей метод додає мапу з підтримуваними мовами до моделі
     * для КОЖНОГО запиту. Він повністю сумісний з Java 8.
     */
    @ModelAttribute("supportedLangs")
    public Map<String, Map<String, String>> getSupportedLangs() {
        Map<String, Map<String, String>> supportedLangs = new LinkedHashMap<>();

        // Створення мапи для української мови
        Map<String, String> ukMap = new HashMap<>();
        ukMap.put("name", "Українська");
        ukMap.put("flag", "🇺🇦");
        ukMap.put("short", "UA");
        supportedLangs.put("uk", ukMap);

        // Створення мапи для англійської мови
        Map<String, String> enMap = new HashMap<>();
        enMap.put("name", "English");
        enMap.put("flag", "🇬🇧");
        enMap.put("short", "EN");
        supportedLangs.put("en", enMap);

        // Створення мапи для німецької мови
        Map<String, String> deMap = new HashMap<>();
        deMap.put("name", "Deutsch");
        deMap.put("flag", "🇩🇪");
        deMap.put("short", "DE");
        supportedLangs.put("de", deMap);

        return supportedLangs;
    }
}
