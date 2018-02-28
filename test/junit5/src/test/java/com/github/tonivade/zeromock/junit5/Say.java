package com.github.tonivade.zeromock.junit5;

import static tonivade.equalizer.Equalizer.equalizer;

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
    return equalizer(this)
        .append((a, b) -> Objects.equals(a.message, b.message))
        .applyTo(obj);
  }

  @Override
  public String toString() {
    return "Say(message=" + message + ")";
  }
}
