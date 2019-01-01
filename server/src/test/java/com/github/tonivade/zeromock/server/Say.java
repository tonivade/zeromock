/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.typeclasses.Eq.comparing;

import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;

import com.github.tonivade.purefun.typeclasses.Equal;

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
    return Equal.of(this)
        .append(comparing(Say::getMessage))
        .applyTo(obj);
  }

  @Override
  public String toString() {
    return "Say(message=" + message + ")";
  }
}
