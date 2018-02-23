package com.github.tonivade.zeromock;

import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "say")
public final class Say {
  private String message;
  
  public Say() { }

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
    if (obj == null)
      return false;
    if (this == obj)
      return true;
    if (getClass() != obj.getClass())
      return false;
    return Objects.equals(((Say) obj).message, this.message);
  }

  @Override
  public String toString() {
    return "Say(message=" + message + ")";
  }
}
