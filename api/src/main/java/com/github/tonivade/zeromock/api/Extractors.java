/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import com.github.tonivade.zeromock.core.Function1;

public final class Extractors {
  
  private Extractors() {}

  public static Function1<HttpRequest, HttpRequest> identity() {
    return Function1.identity();
  }

  public static Function1<HttpRequest, Bytes> body() {
    return HttpRequest::body;
  }

  public static Function1<HttpRequest, String> queryParam(String name) {
    return request -> request.param(name);
  }

  public static Function1<HttpRequest, String> pathParam(int position) {
    return request -> request.pathParam(position);
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
}
