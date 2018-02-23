/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Bytes.asString;
import static java.util.Objects.requireNonNull;

import java.util.Objects;

public final class HttpResponse {

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
    if (obj == null)
      return false;
    if (this == obj)
      return true;
    if (getClass() != obj.getClass())
      return false;
    HttpResponse other = (HttpResponse) obj;
    return Objects.equals(other.status, this.status) 
        && Objects.equals(other.body, this.body) 
        && Objects.equals(other.headers, this.headers);
  }
  
  @Override
  public String toString() {
    return "HttpResponse(" + status + " " + asString(body) + ")";
  }
}
