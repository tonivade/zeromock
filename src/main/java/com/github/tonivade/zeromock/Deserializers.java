/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import java.nio.ByteBuffer;
import java.util.function.Function;

public final class Deserializers {

  private Deserializers() {}
  
  public static Function<ByteBuffer, Object> plain() {
    return input -> new String(input.array(), IOUtils.UTF8);
  }
}
