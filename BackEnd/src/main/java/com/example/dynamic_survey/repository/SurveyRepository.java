package com.example.dynamic_survey.repository;

import com.example.dynamic_survey.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    @Query("SELECT s FROM Survey s WHERE s.status = 'PUBLISHED' " +
           "AND s.startDate <= CURRENT_DATE AND s.endDate >= CURRENT_DATE")

    List<Survey> findActiveSurveys();

    @Query("SELECT s FROM Survey s WHERE " +
            "(:title IS NULL OR s.title LIKE %:title%) AND " +
            "(:startDate IS NULL OR s.startDate >= :startDate) AND " +
            "(:endDate IS NULL OR s.endDate <= :endDate)")
    List<Survey> findByFilters(@Param("title") String title,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);
}
