package com.empManagement.empManagement.controller;

import com.empManagement.empManagement.entity.Employee;
import com.empManagement.empManagement.entity.User;
import com.empManagement.empManagement.service.EmployeeService;
import com.empManagement.empManagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String viewProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        System.out.println("Current username: " + username); // Debug log

        User user = userService.findByUsername(username);
        System.out.println("User found: " + (user != null ? user.getUsername() : "null")); // Debug log

        Employee employee = null;

        if (user != null && user.getEmployeeId() != null) {
            employee = employeeService.getEmployeeById(user.getEmployeeId());
            System.out.println("Employee found: " + (employee != null ? employee.getFull_name() : "null")); // Debug log
        }

        model.addAttribute("user", user);
        model.addAttribute("employee", employee);
        model.addAttribute("activeTab", "profile");

        return "pages/profile/view";
    }

    @GetMapping("/edit")
    public String editProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.findByUsername(username);

        model.addAttribute("user", user);
        model.addAttribute("activeTab", "profile");

        return "pages/profile/edit";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User user,
            RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User existingUser = userService.findByUsername(username);
            existingUser.setEmail(user.getEmail());
            existingUser.setFullName(user.getFullName());
            existingUser.setPhoneNumber(user.getPhoneNumber());

            userService.updateUser(existingUser);

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("activeTab", "profile");
        return "pages/profile/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.findByUsername(username);

        // Validate current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
            return "redirect:/profile/change-password";
        }

        // Validate new password match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/profile/change-password";
        }

        // Update password
        userService.changePassword(username, newPassword);

        redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        return "redirect:/profile";
    }
}