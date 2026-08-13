package com.example.dynamic_survey.controller;

import com.example.dynamic_survey.dto.LoginRequest;
import com.example.dynamic_survey.dto.RegisterRequest;
import com.example.dynamic_survey.service.AuthService;
import com.example.dynamic_survey.vo.AppResponse;
import com.example.dynamic_survey.vo.RspCode;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public AppResponse<?> login(@Valid @RequestBody LoginRequest req) {
        try { return authService.authenticateUser(req); }
        catch (Exception e) {
            return AppResponse.error(RspCode.UNAUTHORIZED, "帳號或密碼錯誤");
        }
    }
    @PostMapping("/register")
    public AppResponse<?> register(@Valid @RequestBody RegisterRequest req) {
        return authService.registerUser(req);
    }
}
