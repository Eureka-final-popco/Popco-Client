package com.popcoclient.exception.business;

import com.popcoclient.exception.BaseException;
import com.popcoclient.exception.ErrorCode;

public class UserNotFoundException extends BaseException {
  public UserNotFoundException() {
    super(ErrorCode.USER_NOT_FOUND);
  }

  public UserNotFoundException(String message) {
    super(ErrorCode.USER_NOT_FOUND, message);
  }
}