/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Extractors.asJson;

import java.nio.ByteBuffer;
import java.util.function.Function;

import com.google.gson.JsonElement;

public final class Deserializers {
  
  private Deserializers() {}
  
  public static Function<ByteBuffer, JsonElement> json() {
    return plain().andThen(asJson());
  }
  
  public static Function<ByteBuffer, String> plain() {
    return Bytes::asString;
  }
}
