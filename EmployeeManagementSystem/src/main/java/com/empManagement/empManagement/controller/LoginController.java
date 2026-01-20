package com.empManagement.empManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping({ "/login", "/auth-login" })
    public String showLoginPage() {
        return "auth-login";
    }
}