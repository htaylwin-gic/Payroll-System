package com.empManagement.empManagement.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class RequestControllerAdvice {

    @ModelAttribute("request")
    public HttpServletRequest addRequestToModel(HttpServletRequest request) {
        return request;
    }

    @ModelAttribute("servletPath")
    public String addServletPath(HttpServletRequest request) {
        return request.getServletPath();
    }
}