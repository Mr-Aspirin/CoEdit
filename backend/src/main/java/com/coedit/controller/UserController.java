package com.coedit.controller;

import com.coedit.common.Result;
import com.coedit.common.UserSessionManager;
import com.coedit.dto.LoginRequest;
import com.coedit.dto.RegisterRequest;
import com.coedit.dto.ResetPasswordRequest;
import com.coedit.dto.SecurityUpdateRequest;
import com.coedit.dto.SecurityVerifyRequest;
import com.coedit.dto.UserDto;
import com.coedit.entity.User;
import com.coedit.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*") 
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserSessionManager sessionManager;

    @PostMapping("/login")
    public Result<UserDto> login(@RequestBody LoginRequest request) {
        UserDto user = userService.login(request);
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        sessionManager.saveUser(token, user);
        return Result.success(user);
    }

    @PostMapping("/register")
    public Result<UserDto> register(@RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @GetMapping("/profile")
    public Result<UserDto> getProfile(@RequestParam Long userId) {
        return Result.success(userService.getUserProfile(userId));
    }

    @PutMapping("/profile")
    public Result<UserDto> updateProfile(@RequestBody UserDto userDto) {
        return Result.success(userService.updateUserProfile(userDto.getId(), userDto));
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null) {
            sessionManager.removeUser(token);
        }
        return Result.success("Logged out");
    }
    
    @GetMapping("/current")
    public Result<UserDto> getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        UserDto user = sessionManager.getUser(token);
        if (user == null) {
            return Result.error(401, "Not logged in");
        }
        
        return Result.success(userService.getUserProfile(user.getId()));
    }

    @PostMapping("/verify-security")
    public Result<Boolean> verifySecurity(@RequestBody SecurityVerifyRequest request) {
        return Result.success(userService.verifySecurityAnswer(request.getUserId(), request.getAnswer()));
    }

    @PostMapping("/update-security")
    public Result<String> updateSecurity(@RequestBody SecurityUpdateRequest request) {
        userService.updateSecurity(request.getUserId(), request.getNewPassword(), request.getNewQuestion(), request.getNewAnswer());
        return Result.success("Security updated successfully");
    }

    @GetMapping("/security-question")
    public Result<String> getSecurityQuestion(@RequestParam String account) {
        User user = userService.findByAccount(account);
        if (user == null) {
            return Result.error(404, "User not found");
        }
        return Result.success(user.getSecurityQuestion());
    }

    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request.getAccount(), request.getSecurityAnswer(), request.getNewPassword());
            return Result.success("Password reset successfully");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
