package com.empManagement.empManagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String message;

    private String type; // INFO, SUCCESS, WARNING, ERROR

    private String category; // PAYROLL, ATTENDANCE, STATUS, SYSTEM

    @Column(name = "reference_id")
    private String referenceId; // Link to related entity (payroll ID, employee ID, etc.)

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    private String icon;

    private String link; // URL to navigate when clicked
}