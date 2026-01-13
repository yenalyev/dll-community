package admin.controller;


import dto.PriceDTO;
import dto.SubscriptionPlanDTO;
import dto.TranslationDTO;
import entity.enums.Currency;
import entity.order.SubscriptionPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.subscription.SubscriptionPlanService;


import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin/subscription-plans")
@RequiredArgsConstructor
@Slf4j
public class AdminSubscriptionPlanController {

    private final SubscriptionPlanService planService;

    /**
     * Список всіх планів підписок
     */
    @GetMapping
    public String listPlans(Model model) {
        List<SubscriptionPlan> plans = planService.getAllPlans();
        model.addAttribute("plans", plans);
        return "admin/subscription-plans/list";
    }

    /**
     * Форма створення нового плану
     */
    @GetMapping("/new")
    public String createPlanForm(Model model) {
        SubscriptionPlanDTO dto = new SubscriptionPlanDTO();

        // Додати шаблони для трьох мов
        dto.getTranslations().add(createTranslationTemplate("ua"));
        dto.getTranslations().add(createTranslationTemplate("en"));
        dto.getTranslations().add(createTranslationTemplate("de"));

        // Додати шаблони для валют
        dto.getPrices().add(createPriceTemplate(Currency.UAH));
        dto.getPrices().add(createPriceTemplate(Currency.EUR));
        dto.getPrices().add(createPriceTemplate(Currency.USD));

        model.addAttribute("planDTO", dto);
        model.addAttribute("currencies", Currency.values());
        model.addAttribute("isEdit", false);
        return "admin/subscription-plans/form";
    }

    /**
     * Створення нового плану
     */
    @PostMapping
    public String createPlan(@Valid @ModelAttribute("planDTO") SubscriptionPlanDTO dto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("currencies", Currency.values());
            model.addAttribute("isEdit", false);
            return "admin/subscription-plans/form";
        }

        try {
            SubscriptionPlan plan = planService.createPlan(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "План підписки успішно створено!");
            return "redirect:/admin/subscription-plans";
        } catch (Exception e) {
            log.error("Помилка створення плану", e);
            model.addAttribute("errorMessage", "Помилка створення плану: " + e.getMessage());
            model.addAttribute("currencies", Currency.values());
            model.addAttribute("isEdit", false);
            return "admin/subscription-plans/form";
        }
    }

    /**
     * Форма редагування плану
     */
    @GetMapping("/{id}/edit")
    public String editPlanForm(@PathVariable Long id, Model model) {
        try {
            SubscriptionPlan plan = planService.getPlanById(id);
            SubscriptionPlanDTO dto = planService.toDTO(plan);

            model.addAttribute("planDTO", dto);
            model.addAttribute("currencies", Currency.values());
            model.addAttribute("isEdit", true);
            return "admin/subscription-plans/form";
        } catch (Exception e) {
            log.error("Помилка завантаження плану для редагування", e);
            model.addAttribute("errorMessage", "План не знайдено");
            return "redirect:/admin/subscription-plans";
        }
    }

    /**
     * Оновлення плану
     */
    @PostMapping("/{id}")
    public String updatePlan(@PathVariable Long id,
                             @Valid @ModelAttribute("planDTO") SubscriptionPlanDTO dto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("currencies", Currency.values());
            model.addAttribute("isEdit", true);
            return "admin/subscription-plans/form";
        }

        try {
            planService.updatePlan(id, dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "План підписки успішно оновлено!");
            return "redirect:/admin/subscription-plans";
        } catch (Exception e) {
            log.error("Помилка оновлення плану", e);
            model.addAttribute("errorMessage", "Помилка оновлення плану: " + e.getMessage());
            model.addAttribute("currencies", Currency.values());
            model.addAttribute("isEdit", true);
            return "admin/subscription-plans/form";
        }
    }

    /**
     * Видалення плану
     */
    @PostMapping("/{id}/delete")
    public String deletePlan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            planService.deletePlan(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "План підписки успішно видалено!");
        } catch (Exception e) {
            log.error("Помилка видалення плану", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Помилка видалення плану: " + e.getMessage());
        }
        return "redirect:/admin/subscription-plans";
    }

    /**
     * Перемкнути статус активності
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            planService.togglePlanStatus(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Статус плану змінено!");
        } catch (Exception e) {
            log.error("Помилка зміни статусу", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Помилка зміни статусу: " + e.getMessage());
        }
        return "redirect:/admin/subscription-plans";
    }

    // Допоміжні методи
    private TranslationDTO createTranslationTemplate(String lang) {
        TranslationDTO dto = new TranslationDTO();
        dto.setLang(lang);
        return dto;
    }

    private PriceDTO createPriceTemplate(Currency currency) {
        PriceDTO dto = new PriceDTO();
        dto.setCurrency(currency);
        return dto;
    }
}
