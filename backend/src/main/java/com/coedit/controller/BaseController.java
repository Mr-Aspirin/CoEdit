package com.coedit.controller;

import com.coedit.common.UserSessionManager;
import com.coedit.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseController {

    @Autowired
    protected UserSessionManager sessionManager;

    protected UserDto getUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return sessionManager.getUser(token);
    }
}
