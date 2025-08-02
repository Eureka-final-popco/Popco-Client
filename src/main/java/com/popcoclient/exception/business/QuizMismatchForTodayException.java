package com.popcoclient.exception.business;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class QuizMismatchForTodayException extends BusinessException {
  public QuizMismatchForTodayException() {
    super(ErrorCode.QUIZ_ID_MISMATCH_FOR_TODAY);
  }

  public QuizMismatchForTodayException(String message) {
    super(ErrorCode.QUIZ_ID_MISMATCH_FOR_TODAY, message);
  }
}