package com.awbd.vetclinic.config;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class SecurityModelAttributes {

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        return hasAuthority(authentication, "ROLE_ADMIN");
    }

    @ModelAttribute("isEmployee")
    public boolean isEmployee(Authentication authentication) {
        return hasAuthority(authentication, "ROLE_EMPLOYEE");
    }

    @ModelAttribute("isUser")
    public boolean isUser(Authentication authentication) {
        return hasAuthority(authentication, "ROLE_USER");
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }
}
