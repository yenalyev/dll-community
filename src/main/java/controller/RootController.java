package controller;

import entity.enums.Language;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class RootController {

    /**
     * Redirects from the root URL ("/") to the default language's homepage.
     */
    @GetMapping("/")
    public String redirectToDefaultLocale() {
        return "redirect:/" + Language.UK.getCode() + "/";
    }

    /**
     * Acts as a gateway for the login page. Redirects from /login to /uk/login,
     * preserving any query parameters like ?error=true or ?logout=true.
     */
    @GetMapping("/login")
    public String loginGateway(@RequestParam Map<String, String> params) {
        String queryParams = params.isEmpty() ? "" : "?" + params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        return "redirect:/" + Language.UK.getCode() + "/login" + queryParams;
    }

    /**
     * Acts as a gateway for the registration page.
     */
    @GetMapping("/register")
    public String registerGateway() {
        return "redirect:/" + Language.UK.getCode() + "/register";
    }
}
