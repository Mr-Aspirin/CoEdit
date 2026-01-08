package com.coedit.service.impl;

import com.coedit.dto.LoginRequest;
import com.coedit.dto.RegisterRequest;
import com.coedit.dto.UserDto;
import com.coedit.entity.User;
import com.coedit.mapper.UserMapper;
import com.coedit.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDto login(LoginRequest request) {
        User user = userMapper.findByAccount(request.getAccount());
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid account or password");
        }
        return convertToDto(user);
    }

    @Override
    public UserDto register(RegisterRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Name is required");
        }
        if (userMapper.findByAccount(request.getAccount()) != null) {
            throw new RuntimeException("Account already exists");
        }
        User user = new User();
        BeanUtils.copyProperties(request, user);
        userMapper.insert(user);
        return convertToDto(user);
    }

    @Override
    public UserDto getUserProfile(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return convertToDto(user);
    }

    @Override
    public UserDto updateUserProfile(Long userId, UserDto userDto) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (userDto.getEmail() != null) user.setEmail(userDto.getEmail());
        if (userDto.getName() != null) user.setName(userDto.getName());
        if (userDto.getPhone() != null) user.setPhone(userDto.getPhone());
        if (userDto.getAvatar() != null) user.setAvatar(userDto.getAvatar());
        if (userDto.getIntro() != null) user.setIntro(userDto.getIntro());
        
        userMapper.update(user);
        return convertToDto(user);
    }

    @Override
    public boolean verifySecurityAnswer(Long userId, String answer) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return answer != null && answer.equals(user.getSecurityAnswer());
    }

    @Override
    public void updateSecurity(Long userId, String newPassword, String newQuestion, String newAnswer) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(newPassword);
        }
        if (newQuestion != null && !newQuestion.isEmpty()) {
            user.setSecurityQuestion(newQuestion);
        }
        if (newAnswer != null && !newAnswer.isEmpty()) {
            user.setSecurityAnswer(newAnswer);
        }
        userMapper.update(user);
    }

    @Override
    public void resetPassword(String account, String securityAnswer, String newPassword) {
        User user = userMapper.findByAccount(account);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!securityAnswer.equals(user.getSecurityAnswer())) {
            throw new RuntimeException("Incorrect security answer");
        }
        user.setPassword(newPassword);
        userMapper.update(user);
    }
    
    @Override
    public User findByAccount(String account) {
        return userMapper.findByAccount(account);
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}
