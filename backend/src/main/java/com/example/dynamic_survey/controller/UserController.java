package com.example.dynamic_survey.controller;

import com.example.dynamic_survey.service.AuthService;
import com.example.dynamic_survey.vo.AppResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    AuthService authService;

    @GetMapping("/profile")
    public AppResponse<?> getProfile() { return authService.getCurrentUser(); }

    @PutMapping("/profile")
    public AppResponse<?> updateProfile(@RequestBody Map<String, String> updates) {
        return authService.updateProfile(updates);
    }
}
