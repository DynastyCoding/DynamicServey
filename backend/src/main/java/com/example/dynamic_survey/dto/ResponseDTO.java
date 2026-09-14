package com.example.dynamic_survey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ResponseDTO (
        Long surveyId,
        @NotBlank(message = "姓名不可為空") String name,
        @NotBlank(message = "手機不可為空") String phone,
        String email,
        @NotNull(message = "年齡不可為空") Integer age,
        List<AnswerDTO> answers
){}
