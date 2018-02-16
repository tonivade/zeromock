/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.function.Function;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class Deserializers {
  
  private Deserializers() {}
  
  public static Function<ByteBuffer, JsonElement> json() {
    return plain().andThen(asJson());
  }
  
  public static <T> Function<ByteBuffer, T> json(Type type) {
    return plain().andThen(asJson()).andThen(fromJson(type));
  }
  
  public static Function<ByteBuffer, String> plain() {
    return Bytes::asString;
  }
  
  public static Function<String, JsonElement> asJson() {
    return json -> new JsonParser().parse(json);
  }
  
  public static <T> Function<JsonElement, T> fromJson(Type type) {
    return json -> new GsonBuilder().create().fromJson(json, type);
  }
}
