/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.tonivade.purefun.data.ImmutableSet;

import nl.jqno.equalsverifier.EqualsVerifier;

public class HttpHeadersTest {
  @Test
  public void isEmpty() {
    HttpHeaders headers = HttpHeaders.empty();
    
    assertAll("should be empty and should not contains any key",
              () -> assertTrue(headers.isEmpty()),
              () -> assertFalse(headers.contains("key")),
              () -> assertEquals(ImmutableSet.empty(), headers.get("key")));
  }

  @Test
  public void notEmpty() {
    HttpHeaders headers = HttpHeaders.empty().withHeader("key", "value");
    
    assertAll("should not be empty and should contains a key", 
              () -> assertFalse(headers.isEmpty()),
              () -> assertTrue(headers.contains("key")),
              () -> assertEquals(ImmutableSet.of("value"), headers.get("key")));
  }

  @Test
  public void multipleValues() {
    HttpHeaders headers = HttpHeaders.empty().withHeader("key", "value1").withHeader("key", "value2");
    
    assertEquals(ImmutableSet.of("value1", "value2"), headers.get("key"));
  }

  @Test
  public void immutable() {
    HttpHeaders headers = HttpHeaders.empty().withHeader("key", "value");
    headers.get("key").append("other");
   
    assertEquals(ImmutableSet.of("value"), headers.get("key"));
  }
  
  @Test
  public void equalsVerifier() {
    EqualsVerifier.forClass(HttpHeaders.class).verify();
  }
}
