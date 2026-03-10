package com.empManagement.empManagement.repository;

import com.empManagement.empManagement.entity.Notification;
import com.empManagement.empManagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false")
    int countUnreadByUser(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user = :user AND n.isRead = false")
    void markAllAsReadByUser(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.user = :user")
    void deleteAllByUser(@Param("user") User user);

    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, String type);

    List<Notification> findByUserAndCategoryOrderByCreatedAtDesc(User user, String category);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < CURRENT_DATE - 30")
    void deleteOldNotifications();
}