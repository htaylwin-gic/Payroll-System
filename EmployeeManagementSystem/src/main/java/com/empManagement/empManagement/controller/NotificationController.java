package com.empManagement.empManagement.controller;

import com.empManagement.empManagement.entity.Notification;
import com.empManagement.empManagement.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String viewNotifications(Model model) {
        List<Notification> notifications = notificationService.getCurrentUserNotifications();
        int unreadCount = notificationService.getUnreadCount();

        // Calculate today's and this week's notifications
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();

        long todayCount = notifications.stream()
                .filter(n -> n.getCreatedAt() != null && n.getCreatedAt().isAfter(startOfDay))
                .count();

        long weekCount = notifications.stream()
                .filter(n -> n.getCreatedAt() != null && n.getCreatedAt().isAfter(startOfWeek))
                .count();

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("todayCount", todayCount);
        model.addAttribute("weekCount", weekCount);

        return "pages/notifications/view";
    }

    @GetMapping("/{id}")
    public String viewNotification(@PathVariable Long id, Model model) {
        Notification notification = notificationService.getNotificationById(id);

        // Mark as read when viewed
        if (!notification.isRead()) {
            notificationService.markAsRead(id);
        }

        model.addAttribute("notification", notification);
        return "pages/notifications/detail";
    }

    @PostMapping("/{id}/read")
    @ResponseBody
    public String markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return "success";
    }

    @PostMapping("/mark-all-read")
    public String markAllAsRead(RedirectAttributes redirectAttributes) {
        notificationService.markAllAsRead();
        redirectAttributes.addFlashAttribute("success", "All notifications marked as read");
        return "redirect:/notifications";
    }

    @PostMapping("/{id}/delete")
    public String deleteNotification(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        notificationService.deleteNotification(id);
        redirectAttributes.addFlashAttribute("success", "Notification deleted");
        return "redirect:/notifications";
    }

    @PostMapping("/clear-all")
    public String clearAllNotifications(RedirectAttributes redirectAttributes) {
        notificationService.clearAll();
        redirectAttributes.addFlashAttribute("success", "All notifications cleared");
        return "redirect:/notifications";
    }

    @GetMapping("/unread-count")
    @ResponseBody
    public int getUnreadCount() {
        return notificationService.getUnreadCount();
    }
}