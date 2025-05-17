package com.capstone.disc_persona_chat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 요청된 페르소나를 찾을 수 없을 때 발생하는 예외
 * @ResponseStatus(HttpStatus.NOT_FOUND) 어노테이션은 이 예외가 발생했을 때
 * HTTP 404 Not Found 상태 코드를 자동으로 반환하도록 Spring MVC에 지시
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PersonaNotFoundException extends RuntimeException {

    public PersonaNotFoundException(String message) {
        super(message);
    }

    public PersonaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

