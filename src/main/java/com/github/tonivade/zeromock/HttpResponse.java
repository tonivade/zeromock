/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.Objects.requireNonNull;

public final class HttpResponse {

  final HttpStatus statusCode;
  final Object body;
  final HttpHeaders headers;
  
  public HttpResponse(HttpStatus statusCode, Object body, HttpHeaders headers) {
    this.statusCode = requireNonNull(statusCode);
    this.body = body;
    this.headers = requireNonNull(headers);
  }

  public HttpResponse withHeader(String key, String value) {
    return new HttpResponse(statusCode, body, headers.withHeader(key, value));
  }
  
  @Override
  public String toString() {
    return "HttpResponse(" + statusCode + " " + body + ")";
  }
}
