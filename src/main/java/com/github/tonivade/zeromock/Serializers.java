/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import java.nio.ByteBuffer;
import java.util.function.Function;

import com.google.gson.GsonBuilder;

public class Serializers {
  
  private Serializers() {}
  
  public static <T> Function<T, ByteBuffer> json() {
    return Serializers.<T>asJson().andThen(Bytes::asByteBuffer);
  }

  public static <T> Function<T, ByteBuffer> plain() {
    return Serializers.<T>asString().andThen(Bytes::asByteBuffer);
  }
  
  private static <T> Function<T, String> asJson() {
    return value -> new GsonBuilder().create().toJson(value);
  }
  
  private static <T> Function<T, String> asString() {
    return Object::toString;
  }
}
