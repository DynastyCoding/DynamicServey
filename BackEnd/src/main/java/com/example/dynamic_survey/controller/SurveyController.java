package com.example.dynamic_survey.controller;

import com.example.dynamic_survey.dto.ResponseDTO;
import com.example.dynamic_survey.service.SurveyService;
import com.example.dynamic_survey.vo.AppResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/surveys")
public class SurveyController {

    @Autowired
    SurveyService surveyService;

    @GetMapping
    public AppResponse<?> getActiveSurveys(){
        return surveyService.getActiveSurveys();
    }

    @GetMapping("/{id}/details")
    public AppResponse<?> getSurveyDetails(@PathVariable("id") Long id){
        return surveyService.getSurveyDetails(id);
    }

    // === 三步驟作答流程 ===
    @PostMapping("/session-store")
    public AppResponse<?> storeInSession(
            @RequestBody ResponseDTO submission, HttpSession session){
        return surveyService.saveToSession(submission, session);
    }

    @GetMapping("/session-get")
    public AppResponse<?> getFromSession(HttpSession session) {
        return surveyService.getFromSession(session);
    }
    @PostMapping("/confirm")
    public AppResponse<?> confirmSubmit(HttpSession session) {
        return surveyService.commitFromSession(session);
    }
    @PostMapping("/{id}/submit")
    public AppResponse<?> submitResponse(
            @PathVariable("id") Long id, @RequestBody ResponseDTO submission) {
        return surveyService.submitResponse(id, submission);
    }
    @GetMapping("/history")
    public AppResponse<?> getUserHistory() { return surveyService.getUserHistory(); }
}

