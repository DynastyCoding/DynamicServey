package com.example.dynamic_survey.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "options")
@Data
public class Option {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 選項屬於特定題目
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false) private String optionText; // 選項文字
    @Column(nullable = false) private int orderIndex;    // 選項順序
}


