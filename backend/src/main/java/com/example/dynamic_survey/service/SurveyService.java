package com.example.dynamic_survey.service;

import com.example.dynamic_survey.dto.*;
import com.example.dynamic_survey.entity.*;
import com.example.dynamic_survey.repository.SurveyRepository;
import com.example.dynamic_survey.repository.SurveyResponseRepository;
import com.example.dynamic_survey.repository.UserRepository;
import com.example.dynamic_survey.security.UserDetailsImpl;
import com.example.dynamic_survey.vo.AppResponse;
import com.example.dynamic_survey.vo.RspCode;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SurveyService {
    @Autowired
    SurveyRepository surveyRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SurveyResponseRepository responseRepository;

    // 前台作答暫存
    private static final String SURVEY_SESSION_KEY = "TEMP_SURVEY_RESPONSE";
    // 後台編輯暫存
    private static final String ADMIN_EDIT_SESSION_KEY = "TEMP_ADMIN_SURVEY";

    //Entity -> DTO
    private SurveyDTO convertToDTO(Survey s) {
        SurveyDTO dto = new SurveyDTO();
        dto.setId(s.getId()); dto.setTitle(s.getTitle());
        dto.setDescription(s.getDescription());
        dto.setStartDate(s.getStartDate()); dto.setEndDate(s.getEndDate());
        dto.setStatus(s.getStatus());
        dto.setQuestions(s.getQuestions().stream().map(q -> {
            QuestionDTO qDto = new QuestionDTO();
            qDto.setId(q.getId()); qDto.setTitle(q.getTitle()); qDto.setType(q.getType());
            qDto.setRequired(q.isRequired()); qDto.setOrderIndex(q.getOrderIndex());
            qDto.setOptions(q.getOptions().stream().map(o -> {
                OptionDTO oDto = new OptionDTO();
                oDto.setId(o.getId()); oDto.setOptionText(o.getOptionText());
                oDto.setOrderIndex(o.getOrderIndex());
                return oDto;
            }).collect(Collectors.toList()));
            return qDto;
        }).collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public AppResponse<SurveyDTO> saveSurvey(SurveyDTO dto) {
        // 有 id 就是更新，沒有就是新增
        Survey survey = (dto.getId() != null)
                ? surveyRepository.findById(dto.getId()).orElse(new Survey())
                : new Survey();
        survey.setTitle(dto.getTitle());
        survey.setDescription(dto.getDescription());
        survey.setStartDate(dto.getStartDate());
        survey.setEndDate(dto.getEndDate());
        survey.setStatus(dto.getStatus());
        survey.getQuestions().clear();
        for (QuestionDTO qDto : dto.getQuestions()) {
            Question q = new Question();
            q.setSurvey(survey);
            q.setTitle(qDto.getTitle());
            q.setType(qDto.getType());
            q.setRequired(qDto.isRequired());
            q.setOrderIndex(qDto.getOrderIndex());
            if (qDto.getOptions() != null) {
                for (OptionDTO oDto : qDto.getOptions()) {
                    Option o = new Option();
                    o.setQuestion(q);
                    o.setOptionText(oDto.getOptionText());
                    o.setOrderIndex(oDto.getOrderIndex());
                    q.getOptions().add(o);
                }
            }
            survey.getQuestions().add(q);
        }
        return AppResponse.success(convertToDTO(surveyRepository.save(survey)));
    }

    // 後台列表 (含篩選，並標記是否已有作答)
    public AppResponse<List<SurveyDTO>> getSurveysByAdmin(
            String title, LocalDate start, LocalDate end) {
        List<Survey> surveys = surveyRepository.findByFilters(title, start, end);
        return AppResponse.success(surveys.stream().map(s -> {
            SurveyDTO dto = convertToDTO(s);
            dto.setHasResponses(responseRepository.existsBySurveyId(s.getId()));
            return dto;
        }).collect(Collectors.toList()));
    }

    // 單一問卷詳情
    public AppResponse<SurveyDTO> getSurveyDetails(Long id) {
        return surveyRepository.findById(id).map(s -> AppResponse.success(convertToDTO(s)))
                .orElse(AppResponse.error(RspCode.NOT_FOUND));
    }

    // 刪除 (已有作答則禁止)
    @Transactional
    public AppResponse<?> deleteSurvey(Long id) {
        if (responseRepository.existsBySurveyId(id))
            return AppResponse.error(RspCode.PARAM_ERROR, "已有作答紀錄");
        surveyRepository.deleteById(id);
        return AppResponse.success(null);
    }

    // 1. 暫存編輯中的問卷
    public AppResponse<?> saveAdminSurveyToSession(SurveyDTO dto, HttpSession session) {
        session.setAttribute(ADMIN_EDIT_SESSION_KEY, dto);
        return AppResponse.success(null);
    }

    // 2. 取回編輯中的問卷
    public AppResponse<SurveyDTO> getAdminSurveyFromSession(HttpSession session) {
        SurveyDTO dto = (SurveyDTO) session.getAttribute(ADMIN_EDIT_SESSION_KEY);
        if (dto == null) return AppResponse.error(RspCode.NOT_FOUND, "找不到編輯中的資料");
        return AppResponse.success(dto);
    }

    // 3. 確認提交，依按鈕決定發佈或草稿，並清空 Session
    @Transactional
    public AppResponse<SurveyDTO> commitAdminSurveyFromSession(
            boolean isPublish, HttpSession session) {
        SurveyDTO dto = (SurveyDTO) session.getAttribute(ADMIN_EDIT_SESSION_KEY);
        if (dto == null) return AppResponse.error(RspCode.NOT_FOUND);
        dto.setStatus(isPublish ? "PUBLISHED" : "DRAFT");
        AppResponse<SurveyDTO> response = saveSurvey(dto);
        if (response.code() == 200) session.removeAttribute(ADMIN_EDIT_SESSION_KEY);
        return response;
    }

    // 某問卷的所有填寫者清單
    public AppResponse<?> getSurveyResponses(Long id) {
        List<SurveyResponse> responses = responseRepository.findBySurveyIdOrderByIdDesc(id);
        return AppResponse.success(responses.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("responseId", r.getId());
            map.put("userName", r.getName());
            map.put("userEmail", r.getEmail());
            map.put("submittedAt", r.getSubmittedAt());
            return map;
        }).collect(Collectors.toList()));
    }

    // 單一作答者的詳細內容
    public AppResponse<?> getResponseDetail(Long responseId) {
        SurveyResponse response = responseRepository.findById(responseId).orElse(null);
        if (response == null) return AppResponse.error(RspCode.NOT_FOUND);
        Map<String, Object> result = new HashMap<>();
        result.put("responseId", response.getId());
        result.put("userName", response.getName());
        result.put("submittedAt", response.getSubmittedAt());
        result.put("surveyTitle", response.getSurvey().getTitle());
        result.put("details", response.getAnswers().stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("questionTitle", a.getQuestion().getTitle());
            m.put("type", a.getQuestion().getType());
            m.put("answer", a.getAnswerText());
            return m;
        }).collect(Collectors.toList()));
        return AppResponse.success(result);
    }

    //統計：初始化
    public AppResponse<?> getSurveyStats(Long id) {
        Survey survey = surveyRepository.findById(id).orElse(null);
        if (survey == null) return AppResponse.error(RspCode.NOT_FOUND);
        List<SurveyResponse> responses = responseRepository.findBySurveyId(id);
        int totalResponses = responses.size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("surveyId", survey.getId());
        stats.put("surveyTitle", survey.getTitle());
        stats.put("totalResponses", totalResponses);

        List<Map<String, Object>> qStatsList = new ArrayList<>();
        for (Question q : survey.getQuestions()) {
            Map<String, Object> qMap = new HashMap<>();
            qMap.put("questionId", q.getId());
            qMap.put("questionTitle", q.getTitle());
            qMap.put("type", q.getType());
            if (q.getType().equals("TEXT")) {
                // 簡答題：收集所有文字回答
                qMap.put("textAnswers", responses.stream()
                        .flatMap(r -> r.getAnswers().stream())
                        .filter(a -> a.getQuestion().getId().equals(q.getId()))
                        .map(ResponseAnswer::getAnswerText)
                        .filter(Objects::nonNull).collect(Collectors.toList()));
            } else {
                // 選擇題：初始化每個選項的計數為 0
                Map<Long, Map<String, Object>> optMap = new HashMap<>();
                for (Option o : q.getOptions()) {
                    Map<String, Object> oData = new HashMap<>();
                    oData.put("optionText", o.getOptionText());
                    oData.put("count", 0);
                    optMap.put(o.getId(), oData);
                }
                // 累加每個選項被選的次數
                responses.stream().flatMap(r -> r.getAnswers().stream())
                        .filter(a -> a.getQuestion().getId().equals(q.getId()))
                        .flatMap(a -> a.getSelectedOptions().stream())
                        .forEach(o -> {
                            Map<String, Object> oData = optMap.get(o.getId());
                            if (oData != null) oData.put("count", (int) oData.get("count") + 1);
                        });
                // 計算百分比 (四捨五入到小數第一位)
                for (Map<String, Object> oData : optMap.values()) {
                    double pct = totalResponses > 0
                            ? ((int) oData.get("count") * 100.0 / totalResponses) : 0;
                    oData.put("percentage", Math.round(pct * 10.0) / 10.0);
                }
                qMap.put("optionStats", optMap);
            }
            qStatsList.add(qMap);
        }

        stats.put("questionStats", qStatsList);
        return AppResponse.success(stats);
    }

    // 取得進行中的問卷 (首頁用)
    public AppResponse<List<SurveyDTO>> getActiveSurveys() {
        List<Survey> surveys = surveyRepository.findActiveSurveys();
        return AppResponse.success(
                surveys.stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    // 暫存作答至 Session (含重複作答檢查)
    public AppResponse<?> saveToSession(ResponseDTO submission, HttpSession session) {
        if (responseRepository.existsBySurveyIdAndEmail(
                submission.surveyId(), submission.email())) {
            return AppResponse.error(RspCode.DUPLICATE_ERROR, "此 Email 已填寫過本問卷。");
        }
        session.setAttribute(SURVEY_SESSION_KEY, submission);
        return AppResponse.success(null);
    }

    // 從 Session 取回暫存資料 (確認頁用)
    public AppResponse<ResponseDTO> getFromSession(HttpSession session) {
        ResponseDTO data = (ResponseDTO) session.getAttribute(SURVEY_SESSION_KEY);
        if (data == null) return AppResponse.error(RspCode.NOT_FOUND);
        return AppResponse.success(data);
    }

    @Transactional
    public AppResponse<?> commitFromSession(HttpSession session) {
        ResponseDTO submission =
                (ResponseDTO) session.getAttribute(SURVEY_SESSION_KEY);
        if (submission == null) return AppResponse.error(RspCode.NOT_FOUND);

        AppResponse<?> response = submitResponse(submission.surveyId(), submission);
        if (response.code() == 200)
            session.removeAttribute(SURVEY_SESSION_KEY); // 成功後清空暫存
        return response;
    }

    @Transactional
    public AppResponse<?> submitResponse(Long surveyId, ResponseDTO submission) {
        Survey survey = surveyRepository.findById(surveyId).orElse(null);
        if (survey == null) return AppResponse.error(RspCode.NOT_FOUND);

        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey);
        response.setSubmittedAt(LocalDateTime.now());
        response.setName(submission.name());   response.setPhone(submission.phone());
        response.setEmail(submission.email()); response.setAge(submission.age());

        // 若為登入會員，連結帳號 (匿名作答則略過)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {
            UserDetailsImpl ud = (UserDetailsImpl) auth.getPrincipal();
            response.setUser(userRepository.findById(ud.getId()).orElse(null));
        }

        for (AnswerDTO aDto : submission.answers()) {
            ResponseAnswer answer = new ResponseAnswer();
            answer.setSurveyResponse(response);
            Question question = survey.getQuestions().stream()
                    .filter(q -> q.getId().equals(aDto.questionId()))
                    .findFirst().orElse(null);
            if (question == null) continue;
            answer.setQuestion(question);

            if (question.getType().equals("TEXT")) {
                answer.setAnswerText(aDto.answerText());          // 簡答
            } else {
                List<Option> selected = question.getOptions().stream()
                        .filter(o -> aDto.optionIds().contains(o.getId()))
                        .collect(Collectors.toList());
                answer.setSelectedOptions(selected);                 // 選取的選項
                answer.setAnswerText(selected.stream()               // 同時存文字 (方便顯示)
                        .map(Option::getOptionText).collect(Collectors.joining(";")));
            }
            response.getAnswers().add(answer);
        }
        responseRepository.save(response); // cascade 一併存入明細
        return AppResponse.success(null);
    }

    public AppResponse<?> getUserHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken)
            return AppResponse.error(RspCode.UNAUTHORIZED);

        UserDetailsImpl ud = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.findById(ud.getId()).orElse(null);
        List<SurveyResponse> history =
                responseRepository.findByUserOrderBySubmittedAtDesc(user);

        return AppResponse.success(history.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("surveyId", r.getSurvey().getId());
            map.put("surveyTitle", r.getSurvey().getTitle());
            map.put("submittedAt", r.getSubmittedAt());
            return map;
        }).collect(Collectors.toList()));
    }
}