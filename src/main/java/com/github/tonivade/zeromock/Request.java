/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Request {

  final String method;
  final Path path;
  final Object body;
  final Map<String, List<String>> headers;
  final Map<String, String> params;

  public Request(String method, Path path, Object body, 
                 Map<String, List<String>> headers, Map<String, String> params) {
    this.method = requireNonNull(method);
    this.path = requireNonNull(path);
    this.body = body;
    this.headers = unmodifiableMap(headers);
    this.params = unmodifiableMap(params);
  }
  
  public String toUrl() {
    return path + (params.isEmpty() ? "" : paramsToString());
  }
  
  public String paramsToString() {
    return "?" + params.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue()).collect(joining("&"));
  }

  public Request withHeader(String string, String value) {
    Map<String, List<String>> newHeaders = new HashMap<>(headers);
    newHeaders.merge(string, Collections.singletonList(value), (oldValue, newValue) -> {
      List<String> newList = new ArrayList<>(oldValue);
      newList.addAll(newValue);
      return newList;
    });
    return new Request(method, path, body, newHeaders, params);
  }

  public Request dropOneLevel() {
    return new Request(method, path.dropOneLevel(), body, headers, params);
  }

  public Request withParam(String key, String value) {
    Map<String, String> newParams = new HashMap<>(params);
    newParams.put(key, value);
    return new Request(method, path, body, headers, newParams);
  }
  
  @Override
  public String toString() {
    return method + " " + path + paramsToString();
  }
}
