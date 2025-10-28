package admin.dto;

import entity.lesson.LessonPrice;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class LessonPriceDto {

    private Long id;

    @NotNull(message = "Currency is required")
    private LessonPrice.Currency currency;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be positive")
    private Long amount; // В копійках/центах

    // Helper методи для конвертації
    public Double getAmountInMainUnits() {
        return amount != null ? amount / 100.0 : 0.0;
    }

    public void setAmountFromMainUnits(Double mainUnits) {
        this.amount = mainUnits != null ? (long) (mainUnits * 100) : 0L;
    }
}
