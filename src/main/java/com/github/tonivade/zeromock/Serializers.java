/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import static com.github.tonivade.zeromock.Extractors.asString;
import static com.github.tonivade.zeromock.Extractors.toJson;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class Serializers {
  
  private Serializers() {}
  
  public static Function<Object, ByteBuffer> serializer(HttpHeaders headers) {
    if (headers.get("Content-type").contains("application/json")) {
      return json();
    } else if (headers.get("Content-type").contains("text/xml")) {
      // TODO: xml serializer
    }
    return plain();
  }

  private static Function<Object, ByteBuffer> json() {
    return toJson().andThen(plain());
  }

  private static Function<Object, ByteBuffer> plain() {
    return asString().andThen(Bytes::asByteBuffer);
  }
}
