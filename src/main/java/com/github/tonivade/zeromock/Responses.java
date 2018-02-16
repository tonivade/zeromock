/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Bytes.asByteBuffer;
import static com.github.tonivade.zeromock.HttpStatus.BAD_REQUEST;
import static com.github.tonivade.zeromock.HttpStatus.CREATED;
import static com.github.tonivade.zeromock.HttpStatus.FORBIDDEN;
import static com.github.tonivade.zeromock.HttpStatus.INTERNAL_SERVER_ERROR;
import static com.github.tonivade.zeromock.HttpStatus.NOT_FOUND;
import static com.github.tonivade.zeromock.HttpStatus.NO_CONTENT;
import static com.github.tonivade.zeromock.HttpStatus.OK;
import static com.github.tonivade.zeromock.Serializers.plain;

import java.nio.ByteBuffer;

public final class Responses {
  
  private Responses() {}

  public static <T> HttpResponse ok(T body) {
    return ok(plain().apply(body));
  }
  
  public static HttpResponse ok(String body) {
    return ok(asByteBuffer(body));
  }

  public static HttpResponse ok(ByteBuffer body) {
    return new HttpResponse(OK, body);
  }

  public static <T> HttpResponse created(T body) {
    return created(plain().apply(body));
  }

  public static HttpResponse created(String body) {
    return created(asByteBuffer(body));
  }

  public static HttpResponse created(ByteBuffer body) {
    return new HttpResponse(CREATED, body);
  }
  
  public static HttpResponse noContent() {
    return new HttpResponse(NO_CONTENT, null);
  }
  
  public static HttpResponse forbidden() {
    return new HttpResponse(FORBIDDEN, null);
  }

  public static <T> HttpResponse badRequest(T body) {
    return badRequest(plain().apply(body));
  }

  public static HttpResponse badRequest(String body) {
    return badRequest(asByteBuffer(body));
  }

  public static HttpResponse badRequest(ByteBuffer body) {
    return new HttpResponse(BAD_REQUEST, body);
  }

  public static <T> HttpResponse notFound(T body) {
    return notFound(plain().apply(body));
  }

  public static HttpResponse notFound(String body) {
    return notFound(asByteBuffer(body));
  }

  public static HttpResponse notFound(ByteBuffer body) {
    return new HttpResponse(NOT_FOUND, body);
  }

  public static <T> HttpResponse error(T body) {
    return error(plain().apply(body));
  }

  public static HttpResponse error(String body) {
    return error(asByteBuffer(body));
  }

  public static HttpResponse error(ByteBuffer body) {
    return new HttpResponse(INTERNAL_SERVER_ERROR, body);
  }
}
