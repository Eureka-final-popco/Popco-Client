package com.popcoclient.exception.business;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class QuestionNotFoundException extends BusinessException {
    public QuestionNotFoundException() {
        super(ErrorCode.QUESTION_NOT_FOUND);
    }
    public QuestionNotFoundException(String message) {
        super(ErrorCode.QUESTION_NOT_FOUND, message);
    }
}
