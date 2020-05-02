/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.purefun.Function1;
import com.jayway.jsonpath.JsonPath;

import java.lang.reflect.Type;

public final class Extractors {
  
  private Extractors() {}

  public static Function1<HttpRequest, HttpRequest> identity() {
    return Function1.identity();
  }

  public static Function1<HttpRequest, Bytes> body() {
    return HttpRequest::body;
  }

  public static <T> Function1<HttpRequest, T> extract(String jsonPath) {
    return body().andThen(asString()).andThen(jsonPath(jsonPath));
  }

  public static Function1<HttpRequest, String> queryParam(String name) {
    return request -> request.param(name);
  }

  public static Function1<HttpRequest, String> pathParam(int position) {
    return request -> request.pathParam(position);
  }

  public static <T> Function1<HttpRequest, T> jsonTo(Type type) {
    return body().andThen(Deserializers.jsonTo(type));
  }

  public static Function1<Bytes, String> asString() {
    return Bytes::asString;
  }
  
  public static Function1<String, Integer> asInteger() {
    return Integer::parseInt;
  }
  
  public static Function1<String, Long> asLong() {
    return Long::parseLong;
  }

  private static <T> Function1<String, T> jsonPath(String jsonPath) {
    return json -> JsonPath.read(json, jsonPath);
  }
}
