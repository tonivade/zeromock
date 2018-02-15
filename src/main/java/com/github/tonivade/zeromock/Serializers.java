/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import java.nio.ByteBuffer;
import java.util.function.Function;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;

public class Serializers {
  
  private Serializers() {}
  
  public static Function<Object, ByteBuffer> serializer(HttpHeaders headers) {
    if (headers.get("Content-type").contains("application/json")) {
      return asJson().andThen(asString()).andThen(asByteBuffer());
    } else if (headers.get("Content-type").contains("text/xml")) {
      // TODO: xml serializer
    }
    return asString().andThen(asByteBuffer());
  }
  
  private static Function<String, ByteBuffer> asByteBuffer() {
    return Bytes::asByteBuffer;
  }
  
  private static Function<Object, JsonValue> asJson() {
    // TODO: object to json
    return value -> Json.object();
  }

  private static Function<Object, String> asString() {
    return Object::toString;
  }
}
