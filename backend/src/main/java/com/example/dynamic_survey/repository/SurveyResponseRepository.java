package com.example.dynamic_survey.repository;

import com.example.dynamic_survey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.dynamic_survey.entity.User;
import java.util.List;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    // 個人歷史紀錄 (依提交時間新到舊)
    List<SurveyResponse> findByUserOrderBySubmittedAtDesc(User user);

    // 統計用：取得某問卷所有回覆
    List<SurveyResponse> findBySurveyId(Long surveyId);

    // 填寫名單：依 ID 逆序 (最新在前)
    List<SurveyResponse> findBySurveyIdOrderByIdDesc(Long surveyId);

    boolean existsBySurveyId(Long surveyId);                       // 是否有人作答
    boolean existsBySurveyIdAndEmail(Long surveyId, String email); // 此 Email 是否填過
}
