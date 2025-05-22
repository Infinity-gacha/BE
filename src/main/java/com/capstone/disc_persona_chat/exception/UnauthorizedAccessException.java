package com.capstone.disc_persona_chat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 권한이 없는 리소스에 접근할 때 발생하는 예외
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends RuntimeException {

    /**
     * 기본 생성자
     */
    public UnauthorizedAccessException() {
        super("해당 리소스에 접근할 권한이 없습니다.");
    }

    /**
     * 메시지를 포함한 생성자
     * 
     * @param message 예외 메시지
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인을 포함한 생성자
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
