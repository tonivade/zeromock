/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class HttpResponseTest {
  
  @Test
  public void equalsVerifier() {
    EqualsVerifier.forClass(HttpResponse.class).verify();
  }
}
