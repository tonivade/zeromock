/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;

public final class HttpResponse {

  private final HttpStatus status;
  private final ByteBuffer body;
  private final HttpHeaders headers;

  public HttpResponse(HttpStatus status, ByteBuffer body) {
    this(status, body, HttpHeaders.empty());
  }
  
  public HttpResponse(HttpStatus status, ByteBuffer body, HttpHeaders headers) {
    this.status = requireNonNull(status);
    this.body = body;
    this.headers = requireNonNull(headers);
  }
  
  public HttpStatus status() {
    return status;
  }
  
  public ByteBuffer body() {
    return body;
  }
  
  public HttpHeaders headers() {
    return headers;
  }

  public HttpResponse withHeader(String key, String value) {
    return new HttpResponse(status, body, headers.withHeader(key, value));
  }
  
  @Override
  public String toString() {
    return "HttpResponse(" + status + " " + body + ")";
  }
}
