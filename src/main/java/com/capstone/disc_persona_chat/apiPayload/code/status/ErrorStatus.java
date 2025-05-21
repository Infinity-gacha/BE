package com.capstone.disc_persona_chat.apiPayload.code.status;

import com.capstone.disc_persona_chat.apiPayload.code.BaseErrorCode;
import com.capstone.disc_persona_chat.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    //일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    FOOD_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON400", "잘못된 요청입니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON400", "잘못된 요청입니다."),
    // 멤버 관려 에러
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001", "사용자가 없습니다."),
    // 로그인 관련 에러
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "MEMBER4005", "유효하지 않은 토큰입니다."),
    INVALID_PASSWORD(HttpStatus.NOT_FOUND, "COMMON400", "비밀번호가 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason(){
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}