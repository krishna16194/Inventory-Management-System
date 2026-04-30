package com.kr.bill.inventorymangementsystem.config;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Adds the currently-logged-in user's name and initial to every
 * Thymeleaf model automatically, so templates can use
 * ${username} and ${userInitial} without any per-controller code.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute
    public void addUserAttributes(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String name = authentication.getName();
            model.addAttribute("username", name);
            model.addAttribute("userInitial",
                    name != null && !name.isEmpty()
                            ? String.valueOf(name.charAt(0)).toUpperCase()
                            : "U");
        }
    }
}
