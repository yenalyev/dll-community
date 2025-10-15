package entity.enums;

import java.util.Arrays;
import java.util.Optional;

public enum Language {
    UK("uk"),
    EN("en"),
    DE("de");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean isSupported(String code) {
        return Arrays.stream(Language.values())
                .anyMatch(lang -> lang.getCode().equals(code));
    }
}
