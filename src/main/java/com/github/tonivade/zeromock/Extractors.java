/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import java.lang.reflect.Type;
import java.util.function.Function;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class Extractors {
  
  private Extractors() {}

  public static Function<HttpRequest, Object> body() {
    return request -> request.body();
  }

  public static Function<HttpRequest, String> queryParam(String name) {
    return request -> request.param(name);
  }

  public static Function<HttpRequest, String> pathParam(int position) {
    return request -> request.pathParam(position);
  }
  
  public static Function<Object, String> asString() {
    return value -> String.valueOf(value);
  }
  
  public static Function<String, Integer> asInteger() {
    return string -> Integer.parseInt(string);
  }
  
  public static Function<String, JsonElement> asJson() {
    return json -> new JsonParser().parse(json);
  }
  
  public static Function<Object, JsonElement> toJson() {
    return value -> new GsonBuilder().create().toJsonTree(value);
  }
  
  public static <T> Function<JsonElement, T> fromJson(Type type) {
    return json -> new GsonBuilder().create().fromJson(json, type);
  }
}
