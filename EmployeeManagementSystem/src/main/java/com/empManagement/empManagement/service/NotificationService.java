package com.empManagement.empManagement.service;

import com.empManagement.empManagement.entity.Notification;
import com.empManagement.empManagement.entity.User;
import com.empManagement.empManagement.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;

    public List<Notification> getCurrentUserNotifications() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return notificationRepository.findByUserOrderByCreatedAtDesc(currentUser);
        }
        return List.of();
    }

    public List<Notification> getUnreadNotifications() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(currentUser);
        }
        return List.of();
    }

    public int getUnreadCount() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return notificationRepository.countUnreadByUser(currentUser);
        }
        return 0;
    }

    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    public Notification createNotification(Notification notification) {
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    public Notification createNotificationForUser(String username, String title, String message, String type,
            String category) {
        User user = userService.findByUsername(username);
        if (user == null)
            return null;

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setCategory(category);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);

        // Set icon based on type
        switch (type) {
            case "SUCCESS":
                notification.setIcon("fa-check-circle");
                break;
            case "WARNING":
                notification.setIcon("fa-exclamation-triangle");
                break;
            case "ERROR":
                notification.setIcon("fa-times-circle");
                break;
            default:
                notification.setIcon("fa-info-circle");
        }

        return notificationRepository.save(notification);
    }

    public Notification createNotificationForUserId(Long userId, String title, String message, String type,
            String category) {
        Optional<User> userOptional = userService.findUserById(userId);
        if (userOptional.isEmpty())
            return null;

        User user = userOptional.get();

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setCategory(category);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);

        // Set icon based on type
        switch (type) {
            case "SUCCESS":
                notification.setIcon("fa-check-circle");
                break;
            case "WARNING":
                notification.setIcon("fa-exclamation-triangle");
                break;
            case "ERROR":
                notification.setIcon("fa-times-circle");
                break;
            default:
                notification.setIcon("fa-info-circle");
        }

        return notificationRepository.save(notification);
    }

    public void createPayrollNotification(String username, String monthYear) {
        String title = "Payroll Calculated";
        String message = "Payroll for " + monthYear + " has been calculated";
        createNotificationForUser(username, title, message, "SUCCESS", "PAYROLL");
    }

    public void createStatusChangeNotification(String username, String employeeName, String oldStatus,
            String newStatus) {
        String title = "Status Updated";
        String message = "Employee " + employeeName + " status changed from " + oldStatus + " to " + newStatus;
        createNotificationForUser(username, title, message, "INFO", "STATUS");
    }

    @Transactional
    public void markAsRead(Long id) {
        Notification notification = getNotificationById(id);
        if (notification != null && !notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            notificationRepository.markAllAsReadByUser(currentUser);
        }
    }

    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Transactional
    public void clearAll() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            notificationRepository.deleteAllByUser(currentUser);
        }
    }

    @Transactional
    public void deleteOldNotifications() {
        notificationRepository.deleteOldNotifications();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userService.findByUsername(auth.getName());
        }
        return null;
    }
}