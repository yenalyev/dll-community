package controller;

import dto.RegisterDto;
import entity.enums.Language;
import exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.user.UserService;

import javax.validation.Valid;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/{lang}") // Префікс для всіх URL в цьому класі
public class AuthController {

    private final UserService userService;

    /**
     * Показує сторінку входу
     */
    @GetMapping("/login")
    public String loginPage(
            @PathVariable String lang,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String success,
            Model model) {

        if (!Language.isSupported(lang)) {
            return "redirect:/" + Language.UK.getCode() + "/login";
        }

        if (error != null) {
            model.addAttribute("error", true);
            log.debug("Login error occurred");
        }

        if (logout != null) {
            model.addAttribute("logout", true);
            log.debug("User logged out");
        }

        if (success != null) {
            model.addAttribute("success", true);
            log.debug("Registration successful, showing login page");
        }

        return "login";
    }

    /**
     * Показує сторінку реєстрації
     */
    @GetMapping("/register")
    public String registerPage(@PathVariable String lang, Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "register";
    }

    /**
     * Обробляє реєстрацію нового користувача
     */
    @PostMapping("/register")
    public String register(
            @PathVariable String lang,
            @Valid @ModelAttribute("registerDto") RegisterDto registerDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Registration attempt for email: {}", registerDto.getEmail());

        // Перевірка валідації
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors during registration: {}", bindingResult.getAllErrors());
            return "register";
        }

        try {
            // Реєструємо користувача
            userService.registerUser(registerDto);

            log.info("User successfully registered: {}", registerDto.getEmail());

            // Перенаправляємо на сторінку входу з повідомленням про успіх
            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! Please login.");

            return "redirect:/" + lang + "/login?success=true";

        } catch (UserAlreadyExistsException e) {
            log.warn("Registration failed - email already exists: {}", registerDto.getEmail());

            bindingResult.rejectValue("email", "error.registerDto",
                    "Email already registered");

            return "register";

        } catch (Exception e) {
            log.error("Unexpected error during registration", e);

            model.addAttribute("error", "An unexpected error occurred. Please try again.");

            return "register";
        }
    }
}
