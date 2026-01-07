package com.coedit.service;

import com.coedit.entity.Notification;
import java.util.List;

public interface NotificationService {
    void createNotification(Long userId, String content, String type, Long relatedId);
    List<Notification> getUserNotifications(Long userId);
    void deleteNotification(Long id);
    void markAllAsRead(Long userId);
    void markInvitationAsAccepted(Long userId, Long documentId);
    long getUnreadCount(Long userId);
}
