package com.coedit.service.impl;

import com.coedit.entity.Notification;
import com.coedit.entity.DocumentCollaborator;
import com.coedit.mapper.NotificationMapper;
import com.coedit.mapper.CollaboratorMapper;
import com.coedit.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private CollaboratorMapper collaboratorMapper;

    @Override
    public void createNotification(Long userId, String content, String type, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notificationMapper.insert(notification);
    }

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationMapper.findByUserId(userId);
    }

    @Override
    public void deleteNotification(Long id) {
        notificationMapper.deleteById(id);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    public void markInvitationAsAccepted(Long userId, Long documentId) {
        notificationMapper.markAsAccepted(userId, documentId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }
}
