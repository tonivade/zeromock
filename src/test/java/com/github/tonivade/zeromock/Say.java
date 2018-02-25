package com.github.tonivade.zeromock;

import static tonivade.equalizer.Equalizer.equalizer;

import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;

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
    return equalizer(this)
        .append((a, b) -> Objects.equals(a.message, b.message))
        .applyTo(obj);
  }

  @Override
  public String toString() {
    return "Say(message=" + message + ")";
  }
}
