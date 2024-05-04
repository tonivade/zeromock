/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.jayway.jsonpath.Configuration.defaultConfiguration;
import static com.jayway.jsonpath.Option.SUPPRESS_EXCEPTIONS;

import java.lang.reflect.Type;

import com.github.tonivade.purefun.core.Function1;
import com.github.tonivade.purefun.type.Option;
import com.github.tonivade.purefun.type.Try;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

public final class Extractors {

  private static final Configuration CONFIGURATION = defaultConfiguration().addOptions(SUPPRESS_EXCEPTIONS);

  private Extractors() {}

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

  public static <T> Function1<HttpRequest, Try<Option<T>>> jsonTo(Type type) {
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
    return json -> JsonPath.parse(json, CONFIGURATION).read(jsonPath);
  }
}
