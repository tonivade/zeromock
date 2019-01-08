/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.typeclasses.Eq.comparing;
import static com.github.tonivade.zeromock.api.Bytes.asBytes;
import static java.util.Objects.requireNonNull;

import java.util.Objects;

import com.github.tonivade.purefun.typeclasses.Equal;
import com.github.tonivade.zeromock.api.HttpPath.PathElement;

public final class HttpRequest {

  private final HttpMethod method;
  private final HttpPath path;
  private final Bytes body;
  private final HttpHeaders headers;
  private final HttpParams params;

  public HttpRequest(HttpMethod method, HttpPath path) {
    this(method, path, Bytes.empty(), HttpHeaders.empty(), HttpParams.empty());
  }

  public HttpRequest(HttpMethod method, HttpPath path, Bytes body, 
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
  
  public HttpPath path() {
    return path;
  }
  
  public Bytes body() {
    return body;
  }
  
  public HttpHeaders headers() {
    return headers;
  }
  
  public HttpParams params() {
    return params;
  }
  
  public String param(String name) {
    return params.get(name).getOrElseThrow(IllegalArgumentException::new);
  }
  
  public String pathParam(int position) {
    return path.getAt(position).map(PathElement::value).getOrElseThrow(IllegalArgumentException::new);
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
    return withBody(asBytes(body));
  }

  public HttpRequest withBody(Bytes body) {
    return new HttpRequest(method, path, body, headers, params);
  }

  public HttpRequest withParam(String key, String value) {
    return new HttpRequest(method, path, body, headers, params.withParam(key, value));
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(method, path, body, headers, params);
  }
  
  @Override
  public boolean equals(Object obj) {
    return Equal.of(this)
        .append(comparing(HttpRequest::method))
        .append(comparing(HttpRequest::path))
        .append(comparing(HttpRequest::body))
        .append(comparing(HttpRequest::headers))
        .append(comparing(HttpRequest::params))
        .applyTo(obj);
  }
  
  @Override
  public String toString() {
    return "HttpRequest(" + method + " " + toUrl() + ")";
  }
}
