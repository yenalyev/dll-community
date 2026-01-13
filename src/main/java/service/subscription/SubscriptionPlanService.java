package service.subscription;


import dto.PriceDTO;
import dto.SubscriptionPlanDTO;
import dto.TranslationDTO;
import entity.order.SubscriptionPlan;
import entity.order.SubscriptionPlanPrice;
import entity.order.SubscriptionPlanTranslation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.SubscriptionPlanRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;


    /**
     * Отримати всі плани підписок з деталями
     */
    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getAllPlans() {
        // Спочатку отримуємо всі плани
        List<SubscriptionPlan> plans = planRepository.findAllOrdered();

        // Потім завантажуємо колекції окремими запитами (N+1 уникається через @Transactional)
        plans.forEach(plan -> {
            plan.getTranslations().size(); // Ініціалізація lazy колекції
            plan.getPrices().size(); // Ініціалізація lazy колекції
        });

        return plans;
    }

    /**
     * Отримати активні плани підписок
     */
    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getActivePlans() {
        return planRepository.findByIsActiveTrue();
    }

    /**
     * Отримати план за ID з усіма деталями
     */
    @Transactional(readOnly = true)
    public SubscriptionPlan getPlanById(Long id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("План підписки не знайдено"));

        // Ініціалізуємо колекції
        plan.getTranslations().size();
        plan.getPrices().size();

        return plan;
    }

    /**
     * Створити новий план підписки
     */
    @Transactional
    public SubscriptionPlan createPlan(SubscriptionPlanDTO dto) {
        log.info("Створення нового плану підписки на {} днів", dto.getDurationInDays());

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setDurationInDays(dto.getDurationInDays());
        plan.setIsActive(dto.getIsActive());

        // Додати переклади
        for (TranslationDTO translationDTO : dto.getTranslations()) {
            SubscriptionPlanTranslation translation = new SubscriptionPlanTranslation();
            translation.setPlan(plan);
            translation.setLang(translationDTO.getLang());
            translation.setName(translationDTO.getName());
            translation.setDescription(translationDTO.getDescription());
            plan.getTranslations().add(translation);
        }

        // Додати ціни
        for (PriceDTO priceDTO : dto.getPrices()) {
            SubscriptionPlanPrice price = new SubscriptionPlanPrice();
            price.setPlan(plan);
            price.setCurrency(priceDTO.getCurrency());
            price.setAmount(priceDTO.getAmount());
            plan.getPrices().add(price);
        }

        return planRepository.save(plan);
    }

    /**
     * Оновити план підписки
     */
    @Transactional
    public SubscriptionPlan updatePlan(Long id, SubscriptionPlanDTO dto) {
        log.info("Оновлення плану підписки #{}", id);

        SubscriptionPlan plan = getPlanById(id);
        plan.setDurationInDays(dto.getDurationInDays());
        plan.setIsActive(dto.getIsActive());

        // Оновити переклади
        plan.getTranslations().clear();
        for (TranslationDTO translationDTO : dto.getTranslations()) {
            SubscriptionPlanTranslation translation = new SubscriptionPlanTranslation();
            translation.setPlan(plan);
            translation.setLang(translationDTO.getLang());
            translation.setName(translationDTO.getName());
            translation.setDescription(translationDTO.getDescription());
            plan.getTranslations().add(translation);
        }

        // Оновити ціни
        plan.getPrices().clear();
        for (PriceDTO priceDTO : dto.getPrices()) {
            SubscriptionPlanPrice price = new SubscriptionPlanPrice();
            price.setPlan(plan);
            price.setCurrency(priceDTO.getCurrency());
            price.setAmount(priceDTO.getAmount());
            plan.getPrices().add(price);
        }

        return planRepository.save(plan);
    }

    /**
     * Видалити план підписки
     */
    @Transactional
    public void deletePlan(Long id) {
        log.info("Видалення плану підписки #{}", id);
        SubscriptionPlan plan = getPlanById(id);
        planRepository.delete(plan);
    }

    /**
     * Перемкнути статус активності плану
     */
    @Transactional
    public void togglePlanStatus(Long id) {
        SubscriptionPlan plan = getPlanById(id);
        plan.setIsActive(!plan.getIsActive());
        planRepository.save(plan);
        log.info("Статус плану #{} змінено на: {}", id, plan.getIsActive());
    }

    /**
     * Конвертувати Entity в DTO
     */
    public SubscriptionPlanDTO toDTO(SubscriptionPlan plan) {
        SubscriptionPlanDTO dto = new SubscriptionPlanDTO();
        dto.setId(plan.getId());
        dto.setDurationInDays(plan.getDurationInDays());
        dto.setIsActive(plan.getIsActive());

        // Конвертувати переклади
        dto.setTranslations(
                plan.getTranslations().stream()
                        .map(t -> {
                            TranslationDTO tDto = new TranslationDTO();
                            tDto.setId(t.getId());
                            tDto.setLang(t.getLang());
                            tDto.setName(t.getName());
                            tDto.setDescription(t.getDescription());
                            return tDto;
                        })
                        .collect(Collectors.toList())
        );

        // Конвертувати ціни
        dto.setPrices(
                plan.getPrices().stream()
                        .map(p -> {
                            PriceDTO pDto = new PriceDTO();
                            pDto.setId(p.getId());
                            pDto.setCurrency(p.getCurrency());
                            pDto.setAmount(p.getAmount());
                            return pDto;
                        })
                        .collect(Collectors.toList())
        );

        return dto;
    }
}
