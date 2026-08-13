import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Survey } from '../models/survey.model';
import { SurveyStats } from '../models/survey-stats.model';

@Injectable({ providedIn: 'root' })
export class SurveyService {
  private http = inject(HttpClient);
  private readonly ADMIN_API_URL  = 'http://localhost:8080/api/admin/surveys';
  private readonly PUBLIC_API_URL = 'http://localhost:8080/api/surveys';

  /** 取得所有已發布的進行中問卷 (前台首頁用) */
  getActiveSurveys(): Observable<Survey[]> {
    return this.http.get<any>(this.PUBLIC_API_URL).pipe(map(res => res.data));
  }

  /** 後台列表查詢 (支援 title / startDate / endDate 篩選) */
  getAllSurveys(title?: string, startDate?: string, endDate?: string): Observable<Survey[]> {
    const params = this.buildFilterParams(title, startDate, endDate);
    return this.http.get<any>(this.ADMIN_API_URL, { params }).pipe(map(res => res.data));
  }

  /** 建構查詢參數 (HttpParams) */
  private buildFilterParams(title?: string, startDate?: string, endDate?: string): HttpParams {
    let params = new HttpParams();
    if (title)     params = params.set('title', title);
    if (startDate) params = params.set('startDate', startDate);
    if (endDate)   params = params.set('endDate', endDate);
    return params;
  }

  /** 後台取得單一問卷詳細資料 */
  getAdminSurveyById(id: number): Observable<Survey> {
    return this.http.get<any>(`${this.ADMIN_API_URL}/${id}`).pipe(map(res => res.data));
  }

  /** 前台取得單一問卷詳細資料 (含題目與選項) */
  getSurveyById(id: number): Observable<Survey> {
    return this.http.get<any>(`${this.PUBLIC_API_URL}/${id}/details`)
      .pipe(map(res => res.data));
  }

  /** 取得問卷的統計數據 */
  getSurveyStats(id: number): Observable<SurveyStats> {
    return this.http.get<any>(`${this.ADMIN_API_URL}/${id}/stats`)
      .pipe(map(res => res.data));
  }

  /** 取得特定問卷的所有填寫者清單 (後台查看回饋列表) */
  getSurveyResponses(surveyId: number): Observable<any[]> {
    return this.http.get<any>(`${this.ADMIN_API_URL}/${surveyId}/responses`)
      .pipe(map(res => res.data));
  }

  /** 取得當前登入者的填答歷史紀錄 */
  getUserHistory(): Observable<any[]> {
    return this.http.get<any>(`${this.PUBLIC_API_URL}/history`).pipe(map(res => res.data));
  }

  /** 取得單筆作答的詳細內容 (查看回饋點擊詳情用) */
  getResponseDetail(responseId: number): Observable<any> {
    return this.http.get<any>(`${this.ADMIN_API_URL}/response-detail/${responseId}`)
      .pipe(map(res => res.data));
  }

  /** 前台作答暫存 (儲存至 Session) */
  saveToSession(response: any): Observable<any> {
    return this.http.post<any>(`${this.PUBLIC_API_URL}/session-store`, response);
  }

  /** 前台確認送出 (將暫存的作答資料正式寫入資料庫) */
  confirmSubmit(): Observable<any> {
    return this.http.post<any>(`${this.PUBLIC_API_URL}/confirm`, {});
  }

  /** 後台編輯暫存 (編輯中的問卷存入 Session) */
  saveAdminSurveyToSession(survey: Survey): Observable<any> {
    return this.http.post<any>(`${this.ADMIN_API_URL}/session-store`, survey);
  }

  /** 後台確認送出 (將編輯中的問卷正式存檔，isPublish 決定狀態為 PUBLISHED 或 DRAFT) */
  confirmAdminSubmit(isPublish: boolean): Observable<any> {
    return this.http.post<any>(
      `${this.ADMIN_API_URL}/confirm-commit?isPublish=${isPublish}`, {});
  }

  /** 新增或更新問卷 (後台儲存) */
  saveSurvey(survey: Survey): Observable<Survey> {
    const request = survey.id
      ? this.http.put<any>(`${this.ADMIN_API_URL}/${survey.id}`, survey)
      : this.http.post<any>(this.ADMIN_API_URL, survey);
    return request.pipe(map(res => res.data));
  }

  /** 刪除問卷 (後台) */
  deleteSurvey(id: number): Observable<void> {
    return this.http.delete<any>(`${this.ADMIN_API_URL}/${id}`).pipe(map(res => res.data));
  }
}
