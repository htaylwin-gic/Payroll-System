package com.empManagement.empManagement.controller;

import com.empManagement.empManagement.entity.SystemSettings;
import com.empManagement.empManagement.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/settings")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @GetMapping
    public String viewSettings(Model model) {
        SystemSettings settings = settingsService.getSettings();
        model.addAttribute("settings", settings);
        model.addAttribute("activeTab", "general");
        return "pages/settings/view";
    }

    @GetMapping("/general")
    public String generalSettings(Model model) {
        SystemSettings settings = settingsService.getSettings();
        model.addAttribute("settings", settings);
        model.addAttribute("activeTab", "general");
        return "pages/settings/general";
    }

    @PostMapping("/general/update")
    public String updateGeneralSettings(@ModelAttribute SystemSettings settings,
            RedirectAttributes redirectAttributes) {
        try {
            settingsService.updateSettings(settings);
            redirectAttributes.addFlashAttribute("success", "General settings updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating settings: " + e.getMessage());
        }
        return "redirect:/settings";
    }

    @GetMapping("/notifications")
    public String notificationSettings(Model model) {
        SystemSettings settings = settingsService.getSettings();
        Map<String, Boolean> notificationPrefs = settings.getNotificationPreferences();

        model.addAttribute("settings", settings);
        model.addAttribute("preferences", notificationPrefs);
        model.addAttribute("activeTab", "notifications");
        return "pages/settings/notifications";
    }

    @PostMapping("/notifications/update")
    public String updateNotificationSettings(@RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes) {
        try {
            Map<String, Boolean> preferences = new HashMap<>();
            preferences.put("emailNotifications", params.containsKey("emailNotifications"));
            preferences.put("systemNotifications", params.containsKey("systemNotifications"));
            preferences.put("payrollAlerts", params.containsKey("payrollAlerts"));
            preferences.put("attendanceReminders", params.containsKey("attendanceReminders"));
            preferences.put("statusChanges", params.containsKey("statusChanges"));

            settingsService.updateNotificationPreferences(preferences);
            redirectAttributes.addFlashAttribute("success", "Notification settings updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating notification settings: " + e.getMessage());
        }
        return "redirect:/settings/notifications";
    }

    @GetMapping("/security")
    public String securitySettings(Model model) {
        SystemSettings settings = settingsService.getSettings();
        model.addAttribute("settings", settings);
        model.addAttribute("activeTab", "security");
        return "pages/settings/security";
    }

    @PostMapping("/security/update")
    public String updateSecuritySettings(@RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes) {
        try {
            Map<String, Object> securitySettings = new HashMap<>();
            securitySettings.put("sessionTimeout", params.get("sessionTimeout"));
            securitySettings.put("maxLoginAttempts", params.get("maxLoginAttempts"));
            securitySettings.put("twoFactorAuth", params.containsKey("twoFactorAuth"));

            settingsService.updateSecuritySettings(securitySettings);
            redirectAttributes.addFlashAttribute("success", "Security settings updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating security settings: " + e.getMessage());
        }
        return "redirect:/settings/security";
    }

    @GetMapping("/backup")
    public String backupSettings(Model model) {
        List<Map<String, String>> backups = settingsService.getBackupHistory();
        model.addAttribute("backups", backups);
        model.addAttribute("activeTab", "backup");
        return "pages/settings/backup";
    }

    @PostMapping("/backup/create")
    public String createBackup(RedirectAttributes redirectAttributes) {
        try {
            String backupFile = settingsService.createBackup();
            redirectAttributes.addFlashAttribute("success", "Backup created successfully: " + backupFile);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating backup: " + e.getMessage());
        }
        return "redirect:/settings/backup";
    }
}