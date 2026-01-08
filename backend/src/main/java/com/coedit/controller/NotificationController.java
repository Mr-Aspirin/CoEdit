package com.coedit.controller;

import com.coedit.common.Result;
import com.coedit.common.UserSessionManager;
import com.coedit.entity.Notification;
import com.coedit.dto.UserDto;
import com.coedit.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@CrossOrigin(origins = "*")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserSessionManager sessionManager;

    @GetMapping("/list")
    public Result<List<Notification>> list(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        UserDto user = sessionManager.getUser(token);
        if (user == null) return Result.error(401, "Not logged in");
        return Result.success(notificationService.getUserNotifications(user.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return Result.success("Deleted successfully");
    }

    @PostMapping("/mark-all-read")
    public Result<String> markAllAsRead(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        UserDto user = sessionManager.getUser(token);
        if (user == null) return Result.error(401, "Not logged in");
        notificationService.markAllAsRead(user.getId());
        return Result.success("Marked all as read");
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        UserDto user = sessionManager.getUser(token);
        if (user == null) return Result.error(401, "Not logged in");
        return Result.success(notificationService.getUnreadCount(user.getId()));
    }
}
