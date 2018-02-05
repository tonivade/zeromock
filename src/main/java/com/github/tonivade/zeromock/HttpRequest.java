/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.Objects.requireNonNull;

public final class HttpRequest {

  final HttpMethod method;
  final Path path;
  final Object body;
  final HttpHeaders headers;
  final HttpParams params;

  public HttpRequest(HttpMethod method, Path path, Object body, 
                     HttpHeaders headers, HttpParams params) {
    this.method = requireNonNull(method);
    this.path = requireNonNull(path);
    this.body = body;
    this.headers = headers;
    this.params = params;
  }
  
  public String toUrl() {
    return path.toPath() + (params.isEmpty() ? "" : params.paramsToString());
  }

  public HttpRequest withHeader(String key, String value) {
    return new HttpRequest(method, path, body, headers.withHeader(key, value), params);
  }

  public HttpRequest dropOneLevel() {
    return new HttpRequest(method, path.dropOneLevel(), body, headers, params);
  }

  public HttpRequest withParam(String key, String value) {
    return new HttpRequest(method, path, body, headers, params.withParam(key, value));
  }
  
  @Override
  public String toString() {
    return "HttpRequest(" + method + " " + toUrl() + ")";
  }
}
