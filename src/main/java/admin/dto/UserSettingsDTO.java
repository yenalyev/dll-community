package admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSettingsDTO {
    private String interfaceLanguage;
    private Boolean wantsNewsletter;
    private String theme;
}