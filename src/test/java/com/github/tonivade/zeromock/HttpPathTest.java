package com.github.tonivade.zeromock;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class HttpPathTest {
  
  @Test
  public void equalsVerifier() {
    EqualsVerifier.forClass(HttpPath.class).verify();
  }
}
