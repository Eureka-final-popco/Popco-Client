package com.popcoclient.exception.business;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class UserDetailAlreadyExistsException extends BusinessException {
  public UserDetailAlreadyExistsException() {
    super(ErrorCode.USER_DETAIL_ALREADY_EXISTS);
  }

  public UserDetailAlreadyExistsException(String message) {
    super(ErrorCode.USER_DETAIL_ALREADY_EXISTS, message);
  }
}