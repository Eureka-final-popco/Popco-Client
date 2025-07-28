package com.popcoclient.exception.business;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class CollectionNotFoundException extends BusinessException {
  public CollectionNotFoundException() {
    super(ErrorCode.COLLECTION_NOT_FOUND);
  }

  public CollectionNotFoundException(String message) {
    super(ErrorCode.COLLECTION_NOT_FOUND, message);
  }
}