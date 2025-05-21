package com.capstone.disc_persona_chat.apiPayload.exception;

import com.capstone.disc_persona_chat.apiPayload.code.BaseErrorCode;

public class UserHandler extends GeneralException{
    public UserHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
