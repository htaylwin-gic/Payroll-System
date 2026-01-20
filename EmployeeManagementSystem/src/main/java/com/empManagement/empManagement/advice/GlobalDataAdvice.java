package com.empManagement.empManagement.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalDataAdvice {

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Fallback to empty string instead of null to prevent errors
        return (uri != null) ? uri : "";
    }
}