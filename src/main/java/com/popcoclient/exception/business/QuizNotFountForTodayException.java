package com.popcoclient.exception.business;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class QuizNotFountForTodayException extends BusinessException {
  public QuizNotFountForTodayException() {
    super(ErrorCode.QUIZ_ID_MISMATCH_FOR_TODAY);
  }

  public QuizNotFountForTodayException(String message) {
    super(ErrorCode.QUIZ_ID_MISMATCH_FOR_TODAY, message);
  }
}