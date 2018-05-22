/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import static com.github.tonivade.zeromock.core.Equal.equal;

import java.util.Objects;

public final class Say {
  private String message;
  
  public Say(String message) {
    this.message = message;
  }

  @Override
  public int hashCode() {
    return Objects.hash(message);
  }

  @Override
  public boolean equals(Object obj) {
    return equal(this)
        .append((a, b) -> Objects.equals(a.message, b.message))
        .applyTo(obj);
  }

  @Override
  public String toString() {
    return "Say(message=" + message + ")";
  }
}
