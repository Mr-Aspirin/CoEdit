package com.coedit.common;

import com.coedit.dto.UserDto;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserSessionManager {
    private static final Map<String, UserDto> tokenMap = new ConcurrentHashMap<>();

    public void saveUser(String token, UserDto user) {
        if (token == null) return;
        tokenMap.put(token, user);
    }

    public UserDto getUser(String token) {
        if (token == null) return null;
        return tokenMap.get(token);
    }

    public void removeUser(String token) {
        if (token == null) return;
        tokenMap.remove(token);
    }
}
