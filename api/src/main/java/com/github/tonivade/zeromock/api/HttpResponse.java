/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Bytes.asString;
import static java.util.Objects.requireNonNull;

import java.util.Objects;

import com.github.tonivade.purefun.core.Equal;

public final class HttpResponse {

  private static final Equal<HttpResponse> EQUAL = Equal.<HttpResponse>of()
      .comparing(HttpResponse::status)
      .comparing(HttpResponse::body)
      .comparing(HttpResponse::headers);

  private final HttpStatus status;
  private final Bytes body;
  private final HttpHeaders headers;

  public HttpResponse(HttpStatus status, Bytes body) {
    this(status, body, HttpHeaders.empty());
  }

  public HttpResponse(HttpStatus status, Bytes body, HttpHeaders headers) {
    this.status = requireNonNull(status);
    this.body = requireNonNull(body);
    this.headers = requireNonNull(headers);
  }

  public HttpStatus status() {
    return status;
  }

  public Bytes body() {
    return body;
  }

  public HttpHeaders headers() {
    return headers;
  }

  public HttpResponse withHeader(String key, String value) {
    return new HttpResponse(status, body, headers.withHeader(key, value));
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, body, headers);
  }

  @Override
  public boolean equals(Object obj) {
    return EQUAL.applyTo(this, obj);
  }

  @Override
  public String toString() {
    return "HttpResponse(" + status + " " + asString(body) + ")";
  }
}
