/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.HttpStatus.BAD_REQUEST;
import static com.github.tonivade.zeromock.HttpStatus.CREATED;
import static com.github.tonivade.zeromock.HttpStatus.FORBIDDEN;
import static com.github.tonivade.zeromock.HttpStatus.INTERNAL_SERVER_ERROR;
import static com.github.tonivade.zeromock.HttpStatus.NOT_FOUND;
import static com.github.tonivade.zeromock.HttpStatus.NO_CONTENT;
import static com.github.tonivade.zeromock.HttpStatus.OK;

public class Responses {
  private Responses() {}

  public static HttpResponse ok(Object body) {
    return new HttpResponse(OK, body, HttpHeaders.empty());
  }

  public static HttpResponse created(Object body) {
    return new HttpResponse(CREATED, body, HttpHeaders.empty());
  }
  
  public static HttpResponse noContent() {
    return new HttpResponse(NO_CONTENT, null, HttpHeaders.empty());
  }
  
  public static HttpResponse forbidden() {
    return new HttpResponse(FORBIDDEN, null, HttpHeaders.empty());
  }

  public static HttpResponse badRequest(Object body) {
    return new HttpResponse(BAD_REQUEST, body, HttpHeaders.empty());
  }

  public static HttpResponse notFound(Object body) {
    return new HttpResponse(NOT_FOUND, body, HttpHeaders.empty());
  }

  public static HttpResponse error(Object body) {
    return new HttpResponse(INTERNAL_SERVER_ERROR, body, HttpHeaders.empty());
  }
}
