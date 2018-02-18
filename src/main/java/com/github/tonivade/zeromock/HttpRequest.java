/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Bytes.asByteBuffer;
import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;

import com.github.tonivade.zeromock.Path.PathElement;

public final class HttpRequest {

  private final HttpMethod method;
  private final Path path;
  private final ByteBuffer body;
  private final HttpHeaders headers;
  private final HttpParams params;

  public HttpRequest(HttpMethod method, Path path) {
    this(method, path, Bytes.empty(), HttpHeaders.empty(), HttpParams.empty());
  }

  public HttpRequest(HttpMethod method, Path path, ByteBuffer body, 
                     HttpHeaders headers, HttpParams params) {
    this.method = requireNonNull(method);
    this.path = requireNonNull(path);
    this.body = requireNonNull(body);
    this.headers = requireNonNull(headers);
    this.params = requireNonNull(params);
  }
  
  public HttpMethod method() {
    return method;
  }
  
  public Path path() {
    return path;
  }
  
  public ByteBuffer body() {
    return body;
  }
  
  public HttpHeaders headers() {
    return headers;
  }
  
  public HttpParams params() {
    return params;
  }
  
  public String param(String name) {
    return params.get(name).orElseThrow(IllegalArgumentException::new);
  }
  
  public String pathParam(int position) {
    return path.getAt(position).map(PathElement::value).orElseThrow(IllegalArgumentException::new);
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
    return withBody(asByteBuffer(body));
  }

  public HttpRequest withBody(ByteBuffer body) {
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
