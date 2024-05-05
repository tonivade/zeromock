/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit5;

import java.util.Objects;

import com.github.tonivade.purefun.core.Equal;
import com.github.tonivade.purejson.JsonCreator;
import com.github.tonivade.purejson.JsonProperty;

public final class Say {

  private String message;

  @JsonCreator
  public Say(@JsonProperty("message") String message) {
    this.message = message;
  }

  @Override
  public int hashCode() {
    return Objects.hash(message);
  }

  @Override
  public boolean equals(Object obj) {
    return Equal.<Say>of()
        .comparing(s -> s.message)
        .applyTo(this, obj);
  }

  @Override
  public String toString() {
    return "Say(message=" + message + ")";
  }
}
