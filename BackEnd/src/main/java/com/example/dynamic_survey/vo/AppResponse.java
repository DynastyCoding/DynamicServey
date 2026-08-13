package com.example.dynamic_survey.vo;

// record 自動產生建構子與 code() / message() / data() 存取方法
public record AppResponse<T>(int code, String message, T data) {

    public static <T> AppResponse<T> success(T data) {
        return new AppResponse<>(RspCode.SUCCESS.getCode(), RspCode.SUCCESS.getMessage(), data);
    }

    public static <T> AppResponse<T> error(RspCode rspCode) {
        return new AppResponse<>(rspCode.getCode(), rspCode.getMessage(), null);
    }

    public static <T> AppResponse<T> error(RspCode rspCode, String customMessage) {
        // record 沒有 setter，直接用自訂訊息建立新物件
        return new AppResponse<>(rspCode.getCode(), customMessage, null);
    }
}

