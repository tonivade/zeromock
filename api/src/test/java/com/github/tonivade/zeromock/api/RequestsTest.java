/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class RequestsTest {

  @Test
  public void method() {
    assertAll(
        () -> assertEquals(HttpMethod.GET, Requests.get("/path").method()),
        () -> assertEquals(HttpMethod.PUT, Requests.put("/path").method()),
        () -> assertEquals(HttpMethod.PATCH, Requests.patch("/path").method()),
        () -> assertEquals(HttpMethod.DELETE, Requests.delete("/path").method()),
        () -> assertEquals(HttpMethod.POST, Requests.post("/path").method())
        );
  }

  @Test
  public void path() {
    assertAll(
        () -> assertEquals(HttpPath.from("/path"), Requests.get("/path").path()),
        () -> assertEquals(HttpPath.from("/path"), Requests.put("/path").path()),
        () -> assertEquals(HttpPath.from("/path"), Requests.patch("/path").path()),
        () -> assertEquals(HttpPath.from("/path"), Requests.delete("/path").path()),
        () -> assertEquals(HttpPath.from("/path"), Requests.post("/path").path())
        );
  }
}
