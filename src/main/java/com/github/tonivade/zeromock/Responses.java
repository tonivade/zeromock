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

import java.util.function.Function;

public class Responses {
  
  private Responses() {}

  public static Function<HttpRequest, HttpResponse> ok(String body) {
    return request -> new HttpResponse(OK, body, HttpHeaders.empty());
  }

  public static Function<HttpRequest, HttpResponse> ok(Function<HttpRequest, Object> action) {
    return request -> new HttpResponse(OK, action.apply(request), HttpHeaders.empty());
  }
  
  public static Function<HttpRequest, HttpResponse> created(String body) {
    return request -> new HttpResponse(CREATED, body, HttpHeaders.empty());
  }
  
  public static Function<HttpRequest, HttpResponse> created(Function<HttpRequest, Object> action) {
    return request -> new HttpResponse(CREATED, action.apply(request), HttpHeaders.empty());
  }
  
  public static Function<HttpRequest, HttpResponse> noContent() {
    return request -> new HttpResponse(NO_CONTENT, null, HttpHeaders.empty());
  }
  
  public static Function<HttpRequest, HttpResponse> forbidden() {
    return request -> new HttpResponse(FORBIDDEN, null, HttpHeaders.empty());
  }

  public static Function<HttpRequest, HttpResponse> badRequest(String body) {
    return request -> new HttpResponse(BAD_REQUEST, body, HttpHeaders.empty());
  }

  public static Function<HttpRequest, HttpResponse> notFound(String body) {
    return request -> new HttpResponse(NOT_FOUND, body, HttpHeaders.empty());
  }

  public static Function<HttpRequest, HttpResponse> error(String body) {
    return request -> new HttpResponse(INTERNAL_SERVER_ERROR, body, HttpHeaders.empty());
  }
  
  public static Function<HttpResponse, HttpResponse> contentType(String value) {
    return response -> response.withHeader("Content-type", value);
  }
  
  public static Function<HttpResponse, HttpResponse> contentJson() {
    return contentType("application/json");
  }
  
  public static Function<HttpResponse, HttpResponse> contentXml() {
    return contentType("text/xml");
  }
}
