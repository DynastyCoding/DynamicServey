package com.example.dynamic_survey.controller;

import com.example.dynamic_survey.dto.SurveyDTO;
import com.example.dynamic_survey.service.SurveyService;
import com.example.dynamic_survey.vo.AppResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/surveys")
public class AdminSurveyController {
    @Autowired
    SurveyService surveyService;

    @GetMapping
    public AppResponse<?> getSurveys(
            @RequestParam(name="title", required=false) String title,
            @RequestParam(name="startDate", required=false)
            @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name="endDate", required=false)
            @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return surveyService.getSurveysByAdmin(title, startDate, endDate);
    }


    @GetMapping("/{id}")
    public AppResponse<?> getSurveyById(@PathVariable("id") Long id) {
        return surveyService.getSurveyDetails(id);
    }

    @PostMapping("")
    public AppResponse<?> createSurvey(@Valid @RequestBody SurveyDTO dto) {
        return surveyService.saveSurvey(dto);
    }

    @PutMapping("/{id}")
    public AppResponse<?> updateSurvey(@PathVariable("id") Long id,
                                       @Valid @RequestBody SurveyDTO dto) {
        dto.setId(id);
        return surveyService.saveSurvey(dto);
    }

    @DeleteMapping("/{id}")
    public AppResponse<?> deleteSurvey(@PathVariable("id") Long id) {
        return surveyService.deleteSurvey(id);
    }

    // === Session 編輯流程 ===
    @PostMapping("/session-store")
    public AppResponse<?> storeSurveyInSession(
            @RequestBody SurveyDTO dto, HttpSession session) {
        return surveyService.saveAdminSurveyToSession(dto, session);
    }
    @GetMapping("/session-get")
    public AppResponse<?> getSurveyFromSession(HttpSession session) {
        return surveyService.getAdminSurveyFromSession(session);
    }
    @PostMapping("/confirm-commit")
    public AppResponse<?> confirmSurveyCommit(
            @RequestParam(name="isPublish") boolean isPublish, HttpSession session) {
        return surveyService.commitAdminSurveyFromSession(isPublish, session);
    }

    // === 統計與作答明細 ===
    @GetMapping("/{id}/stats")
    public AppResponse<?> getSurveyStats(@PathVariable("id") Long id) {
        return surveyService.getSurveyStats(id);
    }
    @GetMapping("/{id}/responses")
    public AppResponse<?> getSurveyResponses(@PathVariable("id") Long id) {
        return surveyService.getSurveyResponses(id);
    }
    @GetMapping("/response-detail/{responseId}")
    public AppResponse<?> getResponseDetail(@PathVariable("responseId") Long rid) {
        return surveyService.getResponseDetail(rid);
    }
}
