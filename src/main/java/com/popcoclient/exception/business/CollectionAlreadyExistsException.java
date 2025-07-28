package com.popcoclient.exception.business;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class CollectionAlreadyExistsException extends BusinessException {
  public CollectionAlreadyExistsException() {
    super(ErrorCode.COLLECTION_ALREADY_EXISTS);
  }

  public CollectionAlreadyExistsException(String message) {
    super(ErrorCode.COLLECTION_ALREADY_EXISTS, message);
  }
}