package com.coedit.mapper;

import com.coedit.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NotificationMapper {
    int insert(Notification notification);
    int deleteById(Long id);
    List<Notification> findByUserId(Long userId);
    void markAllAsRead(Long userId);
    void markAsAccepted(@Param("userId") Long userId, @Param("relatedId") Long relatedId);
    long countUnread(Long userId);
}
