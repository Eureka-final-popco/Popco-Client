package com.popcoclient.exception.business;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class QuizNotFoundException extends BusinessException {
  public QuizNotFoundException() {super(ErrorCode.QUIZ_NOT_FOUND);}
    public QuizNotFoundException(String message) {
        super(ErrorCode.QUIZ_NOT_FOUND, message);
    }
}
