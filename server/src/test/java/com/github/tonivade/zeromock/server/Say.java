/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import java.util.Objects;

import com.github.tonivade.purefun.core.Equal;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "say")
public final class Say {
  private String message;

  public Say() {}

  public Say(String message) {
    this.message = message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public int hashCode() {
    return Objects.hash(message);
  }

  @Override
  public boolean equals(Object obj) {
    return Equal.<Say>of()
        .comparing(Say::getMessage)
        .applyTo(this, obj);
  }

  @Override
  public String toString() {
    return "Say(message=" + message + ")";
  }
}
