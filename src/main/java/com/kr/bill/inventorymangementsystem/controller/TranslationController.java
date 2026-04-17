package com.kr.bill.inventorymangementsystem.controller;

/**
 * Placeholder controller retained to prevent compilation failures.
 *
 * <p>Internationalisation (i18n) locale switching is handled entirely by
 * Spring MVC infrastructure configured in
 * {@link com.kr.bill.inventorymangementsystem.config.I18nConfig}:</p>
 * <ul>
 *   <li>A {@code SessionLocaleResolver} stores the active locale in the
 *       HTTP session.</li>
 *   <li>A {@code LocaleChangeInterceptor} reads the {@code ?lang=XX} query
 *       parameter on every request and updates the session locale.</li>
 * </ul>
 *
 * <p>No explicit controller action is needed; the language switch is
 * triggered simply by appending {@code ?lang=en} or {@code ?lang=ta} to
 * any page URL (e.g. via the Language dropdown in the navigation bar).</p>
 *
 * @see com.kr.bill.inventorymangementsystem.config.I18nConfig
 */
public class TranslationController {
    // Intentionally empty – see class-level Javadoc.
}