package com.popcoclient.exception.business;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class EmailAlreadyExistsException extends BusinessException {
  public EmailAlreadyExistsException() {
    super(ErrorCode.EMAIL_ALREADY_EXISTS);
  }

  public EmailAlreadyExistsException(String message) {
    super(ErrorCode.EMAIL_ALREADY_EXISTS, message);
  }
}