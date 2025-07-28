package com.popcoclient.exception.business;

import com.popcoclient.exception.BusinessException;
import com.popcoclient.exception.ErrorCode;

public class ContentAlreadyExistsInCollectionException extends BusinessException {
  public ContentAlreadyExistsInCollectionException() {
    super(ErrorCode.CONTENT_ALREADY_EXISTS_IN_COLLECTION);
  }

  public ContentAlreadyExistsInCollectionException(String message) {
    super(ErrorCode.CONTENT_ALREADY_EXISTS_IN_COLLECTION, message);
  }
}