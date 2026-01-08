package com.coedit.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        e.printStackTrace();
        String message = e.getMessage();
        if (message == null) {
            message = "Internal Server Error";
        }
        return Result.error(500, message);
    }
}
