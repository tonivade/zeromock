/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import java.nio.charset.Charset;
import java.util.function.Function;

public class Serializers {
  private Serializers() {}

  private static final Charset UTF8 = Charset.forName("UTF-8");
  
  public static Function<Object, byte[]> plain() {
    return object -> object.toString().getBytes(UTF8);
  }

  public static Function<Object, byte[]> xml() {
    return object -> object.toString().getBytes(UTF8);
  }

  public static Function<Object, byte[]> json() {
    return object -> object.toString().getBytes(UTF8);
  }
}
