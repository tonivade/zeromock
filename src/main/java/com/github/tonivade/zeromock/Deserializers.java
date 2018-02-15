/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static java.util.function.Function.identity;

import java.nio.ByteBuffer;
import java.util.function.Function;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;

public final class Deserializers {
  
  private Deserializers() {}
  
  public static Function<ByteBuffer, Object> deserializer(HttpHeaders headers) {
    if (headers.get("Content-type").contains("application/json")) {
      return asString().andThen(parseJson()).andThen(jsonToObject());
    } else if (headers.get("Content-type").contains("text/xml")) {
      // TODO: xml deserializer
    }
    return asString().andThen(identity());
  }
  
  private static Function<ByteBuffer, String> asString() {
    return Bytes::asString;
  }
  
  private static Function<String, JsonValue> parseJson() {
    return Json::parse;
  }

  private static Function<JsonValue, Object> jsonToObject() {
    // TODO: json to object 
    return json -> json;
  }
}
