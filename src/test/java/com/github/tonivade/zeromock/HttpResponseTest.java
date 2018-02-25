package com.github.tonivade.zeromock;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class HttpResponseTest {
  
  @Test
  public void equalsVerifier() {
    EqualsVerifier.forClass(HttpResponse.class).verify();
  }
}
