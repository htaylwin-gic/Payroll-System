package com.empManagement.empManagement.repository;

import com.empManagement.empManagement.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {

    // Since we only have one settings record
    default SystemSettings getSettings() {
        return findAll().stream().findFirst().orElseGet(() -> {
            SystemSettings settings = new SystemSettings();
            return save(settings);
        });
    }
}