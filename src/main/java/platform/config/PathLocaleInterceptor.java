package platform.config;

import entity.enums.Language;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

public class PathLocaleInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        String[] pathParts = uri.split("/");

        if (pathParts.length > 1) {
            String localeCandidate = pathParts[1];
            // Використовуємо наш enum для перевірки
            if (Language.isSupported(localeCandidate)) {
                LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
                if (localeResolver != null) {
                    localeResolver.setLocale(request, response, new Locale(localeCandidate));
                }
            }
        }
        return true;
    }
}
