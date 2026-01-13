package dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class SubscriptionPlanDTO {

    private Long id;

    @NotNull(message = "Тривалість обов'язкова")
    @Min(value = 1, message = "Мінімальна тривалість - 1 день")
    private Integer durationInDays;

    @NotNull
    private Boolean isActive = true;

    @NotEmpty(message = "Необхідно вказати хоча б один переклад")
    private List<TranslationDTO> translations = new ArrayList<>();

    @NotEmpty(message = "Необхідно вказати хоча б одну ціну")
    private List<PriceDTO> prices = new ArrayList<>();
}
