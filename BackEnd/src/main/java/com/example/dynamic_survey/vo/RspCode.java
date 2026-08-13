package com.example.dynamic_survey.vo;

import lombok.Getter;

/*
 * 集中管理系統中所有回應代碼與訊息，避免出現「魔術數字」。
 */
@Getter
public enum RspCode {
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "參數錯誤"),
    UNAUTHORIZED(401, "尚未登入或憑證無效"),
    FORBIDDEN(403, "權限不足"),
    NOT_FOUND(404, "資源不存在"),
    DUPLICATE_ERROR(409, "資料重複"),
    INTERNAL_SERVER_ERROR(500, "系統內部錯誤");

    private final int code;     // HTTP 狀態碼
    private final String message;

    RspCode(int code, String message) {
        this.code = code;
        this.message = message;

    }
}
