/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.Objects.nonNull;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.function.Function;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class Extractors {
  
  private static final String EMPTY = "";

  private Extractors() {}

  public static Function<HttpRequest, ByteBuffer> body() {
    return request -> request.body();
  }

  public static Function<HttpRequest, String> queryParam(String name) {
    return request -> request.param(name);
  }

  public static Function<HttpRequest, String> pathParam(int position) {
    return request -> request.pathParam(position);
  }
  
  public static <T> Function<T, String> asString() {
    return value -> nonNull(value) ? value.toString() : EMPTY;
  }
  
  public static Function<String, Integer> asInteger() {
    return string -> Integer.parseInt(string);
  }
  
  public static Function<String, JsonElement> asJson() {
    return json -> new JsonParser().parse(json);
  }
  
  public static <T> Function<T, JsonElement> toJson() {
    return value -> new GsonBuilder().create().toJsonTree(value);
  }
  
  public static <T> Function<JsonElement, T> fromJson(Type type) {
    return json -> new GsonBuilder().create().fromJson(json, type);
  }
}
