/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class Serializers {
  
  private Serializers() {}
  
  public static Function<Object, ByteBuffer> plain() {
    return object -> ByteBuffer.wrap(object.toString().getBytes(IOUtils.UTF8));
  }
}
