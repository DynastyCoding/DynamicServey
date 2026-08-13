package com.example.dynamic_survey.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {
    private Long id;

    @NotBlank(message = "題目名稱不可為空")
    @Size(max = 75, message = "題目不可超過75字")
    private String title;

    @NotBlank(message = "題目類型不可為空")
    private String type;
    private boolean required;
    private int orderIndex;

    @Valid
    private List<OptionDTO> options;
}
