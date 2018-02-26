/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Bytes.asBytes;
import static com.github.tonivade.zeromock.Bytes.empty;
import static com.github.tonivade.zeromock.HttpStatus.BAD_REQUEST;
import static com.github.tonivade.zeromock.HttpStatus.CREATED;
import static com.github.tonivade.zeromock.HttpStatus.FORBIDDEN;
import static com.github.tonivade.zeromock.HttpStatus.INTERNAL_SERVER_ERROR;
import static com.github.tonivade.zeromock.HttpStatus.NOT_FOUND;
import static com.github.tonivade.zeromock.HttpStatus.NO_CONTENT;
import static com.github.tonivade.zeromock.HttpStatus.OK;

public final class Responses {
  
  private Responses() {}
  
  public static HttpResponse ok() {
    return ok(empty());
  }
  
  public static HttpResponse ok(String body) {
    return ok(asBytes(body));
  }
  
  public static HttpResponse ok(Bytes body) {
    return new HttpResponse(OK, body);
  }
  
  public static HttpResponse created(String body) {
    return created(asBytes(body));
  }

  public static HttpResponse created(Bytes body) {
    return new HttpResponse(CREATED, body);
  }
  
  public static HttpResponse noContent() {
    return new HttpResponse(NO_CONTENT, empty());
  }
  
  public static HttpResponse forbidden() {
    return new HttpResponse(FORBIDDEN, empty());
  }

  public static HttpResponse badRequest() {
    return badRequest(empty());
  }

  public static HttpResponse badRequest(Bytes body) {
    return new HttpResponse(BAD_REQUEST, body);
  }

  public static HttpResponse notFound() {
    return notFound(empty());
  }

  public static HttpResponse notFound(Bytes body) {
    return new HttpResponse(NOT_FOUND, body);
  }

  public static HttpResponse error() {
    return error(empty());
  }

  public static HttpResponse error(Bytes body) {
    return new HttpResponse(INTERNAL_SERVER_ERROR, body);
  }
}
