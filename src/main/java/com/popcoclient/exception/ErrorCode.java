package com.popcoclient.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다"),

  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
  INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 올바르지 않습니다"),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다"),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "만료된 토큰입니다"),
  LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "LOGIN_REQUIRED", "로그인이 필요합니다"),
  
  // User
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다."),

  // Review
  REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", "리뷰를 찾을 수 없습니다."),
  NOT_MY_REVIEW(HttpStatus.FORBIDDEN, "NOT_MY_REVIEW", "본인의 리뷰만 수정 또는 삭제할 수 있습니다."),
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "올바르지 않은 입력값입니다."),
  ALREADY_REVIEWED(HttpStatus.BAD_REQUEST, "ALREADY_REVIEWED", "이미 해당 영화에 대한 리뷰를 작성하셨습니다."),
  
  // Content
  CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_NOT_FOUND", "콘텐츠를 찾을 수 없습니다."),

  // Auth
  UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_USER", "인증되지 않은 사용자입니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
  REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_MISMATCH", "저장된 리프레시 토큰과 일치하지 않습니다."),
  KAKAO_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO_TOKEN_REQUEST_FAILED", "카카오 토큰 요청 실패"),
  KAKAO_TOKEN_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO_TOKEN_PARSING_FAILED", "카카오 토큰 JSON 파싱 실패"),

  // Auth - Jwt
  REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "만료된 REFRESH 토큰입니다."),
  ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "ACCESS_TOKEN_EXPIRED", "만료된 ACCESS 토큰입니다."),
  INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "INVALID_SIGNATURE", "서명이 올바르지 않은 JWT 토큰입니다."),
  UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "UNSUPPORTED_TOKEN", "지원하지 않는 JWT 토큰 형식입니다."),
  EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "EMPTY_TOKEN", "JWT 토큰이 비어 있거나 잘못되었습니다."),
  REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_NOT_FOUND","리프레시 토큰이 쿠키에 존재하지 않습니다."),

  // Declaration
  DECLARATION_ALREADY_EXISTS(HttpStatus.UNAUTHORIZED, "DECLARATION_ALREADY_EXISTS","이미 해당 리뷰를 신고하였습니다."),


  // Persona
  QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION_NOT_FOUND", "해당 순서의 질문을 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;


  ErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}