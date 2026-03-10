package com.empManagement.empManagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Entity
@Table(name = "system_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // General Settings
    @Column(name = "company_name")
    private String companyName = "Employee Management System";

    @Column(name = "company_email")
    private String companyEmail = "admin@example.com";

    @Column(name = "company_phone")
    private String companyPhone = "+95 1 234 567";

    @Column(name = "company_address")
    private String companyAddress;

    @Column(name = "timezone")
    private String timezone = "Asia/Yangon";

    @Column(name = "date_format")
    private String dateFormat = "yyyy-MM-dd";

    @Column(name = "currency")
    private String currency = "MMK";

    // Notification Settings
    @Column(name = "email_notifications")
    private boolean emailNotifications = true;

    @Column(name = "system_notifications")
    private boolean systemNotifications = true;

    @Column(name = "payroll_alerts")
    private boolean payrollAlerts = true;

    @Column(name = "attendance_reminders")
    private boolean attendanceReminders = true;

    @Column(name = "status_change_alerts")
    private boolean statusChangeAlerts = true;

    // Security Settings
    @Column(name = "session_timeout")
    private Integer sessionTimeout = 30; // minutes

    @Column(name = "max_login_attempts")
    private Integer maxLoginAttempts = 5;

    @Column(name = "password_expiry_days")
    private Integer passwordExpiryDays = 90;

    @Column(name = "two_factor_auth")
    private boolean twoFactorAuth = false;

    // Payroll Settings
    @Column(name = "default_working_days")
    private Integer defaultWorkingDays = 22;

    @Column(name = "overtime_rate")
    private Double overtimeRate = 1.5;

    @Column(name = "tax_rate")
    private Double taxRate = 10.0;

    @Column(name = "ssc_rate")
    private Double sscRate = 5.0;

    // Backup Settings
    @Column(name = "auto_backup")
    private boolean autoBackup = false;

    @Column(name = "backup_frequency")
    private String backupFrequency = "WEEKLY"; // DAILY, WEEKLY, MONTHLY

    @Column(name = "backup_location")
    private String backupLocation = "./backups";

    @Column(name = "last_backup")
    private LocalDateTime lastBackup;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "updated_by")
    private String updatedBy;

    // Store additional preferences as JSON
    @Column(name = "notification_preferences", columnDefinition = "TEXT")
    private String notificationPreferencesJson;

    @Transient
    private Map<String, Boolean> notificationPreferences = new HashMap<>();

    @Column(name = "security_settings", columnDefinition = "TEXT")
    private String securitySettingsJson;

    @Transient
    private Map<String, Object> securitySettings = new HashMap<>();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Map<String, Boolean> getNotificationPreferences() {
        if (notificationPreferences.isEmpty()) {
            notificationPreferences.put("emailNotifications", emailNotifications);
            notificationPreferences.put("systemNotifications", systemNotifications);
            notificationPreferences.put("payrollAlerts", payrollAlerts);
            notificationPreferences.put("attendanceReminders", attendanceReminders);
            notificationPreferences.put("statusChanges", statusChangeAlerts);
        }
        return notificationPreferences;
    }
}