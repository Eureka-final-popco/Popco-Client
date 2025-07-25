package com.popcoclient.exception.business;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class DeclarationAlreadyExistsException extends BusinessException {
  public DeclarationAlreadyExistsException() {
    super(ErrorCode.DECLARATION_ALREADY_EXISTS);
  }

  public DeclarationAlreadyExistsException(String message) {
    super(ErrorCode.DECLARATION_ALREADY_EXISTS, message);
  }
}