package com.coedit.service;

import com.coedit.dto.LoginRequest;
import com.coedit.dto.RegisterRequest;
import com.coedit.dto.UserDto;
import com.coedit.entity.User;

public interface UserService {
    UserDto login(LoginRequest request);
    UserDto register(RegisterRequest request);
    UserDto getUserProfile(Long userId);
    UserDto updateUserProfile(Long userId, UserDto userDto);
    boolean verifySecurityAnswer(Long userId, String answer);
    void updateSecurity(Long userId, String newPassword, String newQuestion, String newAnswer);
    void resetPassword(String account, String securityAnswer, String newPassword);
    User findByAccount(String account);
}
