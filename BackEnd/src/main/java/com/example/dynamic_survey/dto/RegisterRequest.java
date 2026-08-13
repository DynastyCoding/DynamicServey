package com.example.dynamic_survey.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "姓名不可為空")
        String name,

        @NotBlank(message = "電子郵件不可為空")
        @Email(message = "電子郵件格式不正確")
        String email,

        @NotBlank(message = "密碼不可為空")
        @Size(min = 6, message = "密碼長度需至少6位")
        String password,
        String phone
) {}
