/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.HashMap;
import java.util.Map;

public final class HttpRequest {

  final HttpMethod method;
  final Path path;
  final Object body;
  final HttpHeaders headers;
  final Map<String, String> params;

  public HttpRequest(HttpMethod method, Path path, Object body, 
                     HttpHeaders headers, Map<String, String> params) {
    this.method = requireNonNull(method);
    this.path = requireNonNull(path);
    this.body = body;
    this.headers = headers;
    this.params = unmodifiableMap(params);
  }
  
  public String toUrl() {
    return path + (params.isEmpty() ? "" : paramsToString());
  }
  
  public String paramsToString() {
    return "?" + params.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue()).collect(joining("&"));
  }

  public HttpRequest withHeader(String key, String value) {
    return new HttpRequest(method, path, body, headers.withHeader(key, value), params);
  }

  public HttpRequest dropOneLevel() {
    return new HttpRequest(method, path.dropOneLevel(), body, headers, params);
  }

  public HttpRequest withParam(String key, String value) {
    Map<String, String> newParams = new HashMap<>(params);
    newParams.put(key, value);
    return new HttpRequest(method, path, body, headers, newParams);
  }
  
  @Override
  public String toString() {
    return "HttpRequest(" + method + " " + toUrl() + ")";
  }
}
