/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zermock.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.Requests;

import nl.jqno.equalsverifier.EqualsVerifier;

public class HttpRequestTest {
  
  @Test
  public void dropOneLevel() {
    HttpRequest request = Requests.get("/level1/level2/level3").dropOneLevel();
    
    assertEquals(Requests.get("/level2/level3"), request);
  }
  
  @Test
  public void equalsVerifier() {
    EqualsVerifier.forClass(HttpRequest.class).verify();
  }
}
