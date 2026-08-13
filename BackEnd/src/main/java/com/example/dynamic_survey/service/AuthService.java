package com.example.dynamic_survey.service;

import com.example.dynamic_survey.dto.LoginRequest;
import com.example.dynamic_survey.dto.RegisterRequest;
import com.example.dynamic_survey.entity.User;
import com.example.dynamic_survey.repository.UserRepository;
import com.example.dynamic_survey.security.JwtUtil;
import com.example.dynamic_survey.security.UserDetailsImpl;
import com.example.dynamic_survey.vo.AppResponse;
import com.example.dynamic_survey.vo.RspCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JwtUtil jwtUtils;

    public AppResponse<?> registerUser(RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.email())) {
            return AppResponse.error(RspCode.DUPLICATE_ERROR, "錯誤：此電子郵件已被使用！");
        }
        User user = new User();
        user.setEmail(signUpRequest.email());
        user.setName(signUpRequest.name());
        user.setPassword(encoder.encode(signUpRequest.password())); // BCrypt 加密
        user.setPhone(signUpRequest.phone());
        user.setRole("ADMIN"); // 教學版：註冊即為管理員
        userRepository.save(user);

        // 註冊完直接幫使用者登入，回傳 Token
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(signUpRequest.email());
        loginReq.setPassword(signUpRequest.password());
        return authenticateUser(loginReq);
    }

    public AppResponse<?> authenticateUser(LoginRequest loginRequest) {
        // 1. 交給 Spring Security 驗證帳密
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), loginRequest.getPassword()));

        // 2. 登記到 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. 簽發 JWT 並回傳
        String jwt = jwtUtils.generateJwtToken(authentication);
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        return AppResponse.success(response);
    }

    public AppResponse<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal().equals("anonymousUser"))
            return AppResponse.error(RspCode.UNAUTHORIZED);
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return AppResponse.error(RspCode.NOT_FOUND);
        return AppResponse.success(userToMap(user));
    }

    public AppResponse<?> updateProfile(Map<String, String> updates) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user == null) return AppResponse.error(RspCode.NOT_FOUND);
        if (updates.containsKey("name"))  user.setName(updates.get("name"));
        if (updates.containsKey("phone")) user.setPhone(updates.get("phone"));
        if (updates.get("password") != null && !updates.get("password").isEmpty())
            user.setPassword(encoder.encode(updates.get("password")));
        userRepository.save(user);
        return AppResponse.success(userToMap(user));
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("name", user.getName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("role", user.getRole());
        return userInfo; // 注意：不回傳 password
    }


}


