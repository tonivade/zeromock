/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.Objects.requireNonNull;

import com.github.tonivade.zeromock.Path.PathElement;

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
    this.headers = requireNonNull(headers);
    this.params = requireNonNull(params);
  }
  
  public HttpMethod method() {
    return method;
  }
  
  public Path path() {
    return path;
  }
  
  public Object body() {
    return body;
  }
  
  public HttpHeaders headers() {
    return headers;
  }
  
  public HttpParams params() {
    return params;
  }
  
  public Integer paramAsInteger(String param) {
    return params.get(param).map(Integer::parseInt).orElseThrow(IllegalArgumentException::new);
  }
  
  public Integer pathParamAsInteger(int position) {
    return path.getAt(position).map(PathElement::value).map(Integer::parseInt).orElseThrow(IllegalArgumentException::new);
  }
  
  public String toUrl() {
    return path.toPath() + params.toQueryString();
  }

  public HttpRequest dropOneLevel() {
    return new HttpRequest(method, path.dropOneLevel(), body, headers, params);
  }

  public HttpRequest withHeader(String key, String value) {
    return new HttpRequest(method, path, body, headers.withHeader(key, value), params);
  }

  public HttpRequest withBody(String body) {
    return new HttpRequest(method, path, body, headers, params);
  }

  public HttpRequest withParam(String key, String value) {
    return new HttpRequest(method, path, body, headers, params.withParam(key, value));
  }
  
  @Override
  public String toString() {
    return "HttpRequest(" + method + " " + toUrl() + ")";
  }
}
