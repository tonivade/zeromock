/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Requests.get;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ExtractorsTest {

  @Test
  public void extractFound() {
    HttpRequest request = get("/").withBody("{\"title\":\"asdfg\"}");

    String title = Extractors.<String>extract("$.title").apply(request);
    
    assertEquals("asdfg", title);
  }

  @Test
  public void extractNotFound() {
    HttpRequest request = get("/").withBody("{}");

    String title = Extractors.<String>extract("$.title").apply(request);
    
    assertNull(title);
  }
}
