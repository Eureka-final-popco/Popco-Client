package com.popcoclient.exception.business;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class QuizNotFoundForTodayException extends BusinessException {
  public QuizNotFoundForTodayException() {
    super(ErrorCode.QUIZ_NOT_FOUND_FOR_TODAY);
  }

  public QuizNotFoundForTodayException(String message) {
    super(ErrorCode.QUIZ_NOT_FOUND_FOR_TODAY, message);
  }
}