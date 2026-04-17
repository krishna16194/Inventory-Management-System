package com.kr.bill.inventorymangementsystem.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Internationalisation (i18n) configuration for the Inventory Management System.
 *
 * <p>Enables multi-language support through Spring's {@link MessageSource}
 * mechanism. The active locale is stored in the HTTP session and can be
 * changed at any time by appending a {@code lang} query parameter to any URL.</p>
 *
 * <h3>Supported Languages</h3>
 * <table border="1">
 *   <tr><th>Code</th><th>Language</th><th>Properties file</th></tr>
 *   <tr><td>{@code en}</td><td>English (default)</td><td>{@code messages_en.properties}</td></tr>
 *   <tr><td>{@code ta}</td><td>Tamil</td><td>{@code messages_ta.properties}</td></tr>
 * </table>
 *
 * <h3>Switching Language</h3>
 * Append {@code ?lang=ta} or {@code ?lang=en} to any page URL, or use the
 * Language dropdown in the navigation bar. The selection persists for the
 * duration of the browser session.
 *
 * <h3>Adding a New Language</h3>
 * <ol>
 *   <li>Create {@code messages_XX.properties} in {@code src/main/resources/}
 *       (where {@code XX} is the ISO 639-1 language code).</li>
 *   <li>Add a dropdown item in the navbar partial of each Thymeleaf template.</li>
 * </ol>
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /**
     * Configures a session-based locale resolver with English as the default
     * locale. The resolved locale is stored in the user's HTTP session so it
     * persists across page navigations within the same browser session.
     *
     * @return the configured {@link LocaleResolver}
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }

    /**
     * Creates an interceptor that reads the {@code lang} query parameter from
     * every incoming HTTP request and updates the session locale accordingly.
     * Example: {@code /dashboard?lang=ta} switches the UI to Tamil.
     *
     * @return the configured {@link LocaleChangeInterceptor}
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    /**
     * Registers the {@link #localeChangeInterceptor()} with Spring MVC so that
     * it is applied to every request before the controller is invoked.
     *
     * @param registry the Spring MVC interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /**
     * Configures the {@link MessageSource} that resolves message keys used in
     * Thymeleaf templates via the {@code #{key}} expression syntax.
     *
     * <p>Messages are read from {@code messages*.properties} files on the
     * classpath. UTF-8 encoding is enforced so that Tamil and other non-ASCII
     * characters are handled correctly.</p>
     *
     * @return the configured {@link MessageSource}
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}