package com.empManagement.empManagement.service;

import com.empManagement.empManagement.entity.SystemSettings;
import com.empManagement.empManagement.repository.SystemSettingsRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
public class SettingsService {

    @Autowired
    private SystemSettingsRepository settingsRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    public SystemSettings getSettings() {
        return settingsRepository.getSettings();
    }

    @Transactional
    public SystemSettings updateSettings(SystemSettings settings) {
        SystemSettings existing = getSettings();

        // Update fields
        existing.setCompanyName(settings.getCompanyName());
        existing.setCompanyEmail(settings.getCompanyEmail());
        existing.setCompanyPhone(settings.getCompanyPhone());
        existing.setCompanyAddress(settings.getCompanyAddress());
        existing.setTimezone(settings.getTimezone());
        existing.setDateFormat(settings.getDateFormat());
        existing.setCurrency(settings.getCurrency());

        // Notification settings
        existing.setEmailNotifications(settings.isEmailNotifications());
        existing.setSystemNotifications(settings.isSystemNotifications());
        existing.setPayrollAlerts(settings.isPayrollAlerts());
        existing.setAttendanceReminders(settings.isAttendanceReminders());
        existing.setStatusChangeAlerts(settings.isStatusChangeAlerts());

        // Payroll settings
        existing.setDefaultWorkingDays(settings.getDefaultWorkingDays());
        existing.setOvertimeRate(settings.getOvertimeRate());
        existing.setTaxRate(settings.getTaxRate());
        existing.setSscRate(settings.getSscRate());

        // Set updated by
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            existing.setUpdatedBy(auth.getName());
        }

        existing.setUpdatedAt(LocalDateTime.now());

        return settingsRepository.save(existing);
    }

    @Transactional
    public void updateNotificationPreferences(Map<String, Boolean> preferences) {
        SystemSettings settings = getSettings();

        settings.setEmailNotifications(preferences.getOrDefault("emailNotifications", true));
        settings.setSystemNotifications(preferences.getOrDefault("systemNotifications", true));
        settings.setPayrollAlerts(preferences.getOrDefault("payrollAlerts", true));
        settings.setAttendanceReminders(preferences.getOrDefault("attendanceReminders", true));
        settings.setStatusChangeAlerts(preferences.getOrDefault("statusChanges", true));

        // Store as JSON
        try {
            settings.setNotificationPreferencesJson(objectMapper.writeValueAsString(preferences));
        } catch (Exception e) {
            e.printStackTrace();
        }

        settingsRepository.save(settings);
    }

    @Transactional
    public void updateSecuritySettings(Map<String, Object> securitySettings) {
        SystemSettings settings = getSettings();

        settings.setSessionTimeout(Integer.parseInt(securitySettings.getOrDefault("sessionTimeout", "30").toString()));
        settings.setMaxLoginAttempts(
                Integer.parseInt(securitySettings.getOrDefault("maxLoginAttempts", "5").toString()));
        settings.setTwoFactorAuth(
                Boolean.parseBoolean(securitySettings.getOrDefault("twoFactorAuth", "false").toString()));

        // Store as JSON
        try {
            settings.setSecuritySettingsJson(objectMapper.writeValueAsString(securitySettings));
        } catch (Exception e) {
            e.printStackTrace();
        }

        settingsRepository.save(settings);
    }

    public String createBackup() throws IOException {
        SystemSettings settings = getSettings();

        // Create backup directory if it doesn't exist
        String backupLocation = settings.getBackupLocation();
        Path backupPath = Paths.get(backupLocation);
        if (!Files.exists(backupPath)) {
            Files.createDirectories(backupPath);
        }

        // Generate backup filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFile = backupLocation + "/backup_" + timestamp + ".zip";

        // TODO: Implement actual database backup logic here
        // This would typically involve calling database dump commands
        // For now, we'll create a simple text file

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupFile))) {
            // Add a README file
            ZipEntry entry = new ZipEntry("backup_info.txt");
            zos.putNextEntry(entry);
            String info = "Backup created at: " + LocalDateTime.now() + "\n";
            info += "Backup type: System settings and configuration\n";
            zos.write(info.getBytes());
            zos.closeEntry();
        }

        // Update last backup time
        settings.setLastBackup(LocalDateTime.now());
        settingsRepository.save(settings);

        return backupFile;
    }

    public List<Map<String, String>> getBackupHistory() {
        List<Map<String, String>> backups = new ArrayList<>();
        SystemSettings settings = getSettings();

        try {
            Path backupDir = Paths.get(settings.getBackupLocation());
            if (Files.exists(backupDir)) {
                Files.list(backupDir)
                        .filter(path -> path.toString().endsWith(".zip"))
                        .forEach(path -> {
                            Map<String, String> backup = new HashMap<>();
                            backup.put("filename", path.getFileName().toString());
                            try {
                                backup.put("size", Files.size(path) / 1024 + " KB");
                                backup.put("modified", Files.getLastModifiedTime(path).toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            backups.add(backup);
                        });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Sort by filename (timestamp) descending
        backups.sort((a, b) -> b.get("filename").compareTo(a.get("filename")));

        return backups;
    }

    // Helper method to get current username
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}