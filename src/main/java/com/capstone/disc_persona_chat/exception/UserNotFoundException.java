package com.capstone.disc_persona_chat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    /**
     * 기본 생성자
     */
    public UserNotFoundException() {
        super("사용자를 찾을 수 없습니다.");
    }

    /**
     * 메시지를 포함한 생성자
     * 
     * @param message 예외 메시지
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인을 포함한 생성자
     * 
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
