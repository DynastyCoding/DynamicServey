package com.example.dynamic_survey.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "survey_responses")
@Data
public class SurveyResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    // === 作答者基本資訊 (直接存於此，支援免登入作答) ===
    @Column(nullable = false) private String name;   // 姓名
    @Column(nullable = false) private String phone;  // 手機
    @Column(nullable = false) private String email;  // Email (重複作答檢查依據)
    @Column private Integer age;                      // 年齡 (選填)
    @Column(nullable = false) private LocalDateTime submittedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @OneToMany(mappedBy = "surveyResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResponseAnswer> answers = new ArrayList<>();
}