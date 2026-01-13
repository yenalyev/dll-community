package dto;

import entity.enums.Currency;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class PriceDTO {
    private Long id;

    @NotNull(message = "Валюта обов'язкова")
    private Currency currency;

    @NotNull(message = "Сума обов'язкова")
    @Min(value = 1, message = "Мінімальна сума - 1 копійка/цент")
    private Long amount;
}
