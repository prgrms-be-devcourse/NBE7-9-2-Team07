package com.back.pinco.global.globalExceptionHandler;

import com.back.pinco.global.rsData.RsData;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

public class GlobalExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public RsData<Void> handleException(HttpMessageNotReadableException e) {
        return new RsData<Void>(
                "400-2",
                "잘못된 형식의 요청 데이터입니다."
        );
    }
}
