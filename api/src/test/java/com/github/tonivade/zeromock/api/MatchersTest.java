/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.core.Matcher1.is;
import static com.github.tonivade.zeromock.api.Matchers.body;
import static com.github.tonivade.zeromock.api.Matchers.delete;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Matchers.header;
import static com.github.tonivade.zeromock.api.Matchers.json;
import static com.github.tonivade.zeromock.api.Matchers.options;
import static com.github.tonivade.zeromock.api.Matchers.param;
import static com.github.tonivade.zeromock.api.Matchers.patch;
import static com.github.tonivade.zeromock.api.Matchers.post;
import static com.github.tonivade.zeromock.api.Matchers.put;
import static com.github.tonivade.zeromock.api.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MatchersTest {

  @Test
  public void methods() {
    assertAll(
        () -> assertTrue(get("/test").match(Requests.get("/test"))),
        () -> assertTrue(post("/test").match(Requests.post("/test"))),
        () -> assertTrue(delete("/test").match(Requests.delete("/test"))),
        () -> assertTrue(put("/test").match(Requests.put("/test"))),
        () -> assertTrue(patch("/test").match(Requests.patch("/test"))),
        () -> assertTrue(options("/test").match(Requests.options("/test")))
    );
  }

  @Test
  public void parameters() {
    assertAll(
        () -> assertTrue(param("key").match(Requests.get("/test").withParam("key", "value"))),
        () -> assertTrue(param("key", "value").match(Requests.get("/test").withParam("key", "value"))),
        () -> assertTrue(startsWith("/path").match(Requests.get("/path/test"))),
        () -> assertTrue(get("/test/:id").match(Requests.get("/test/1"))),
        () -> assertTrue(body("asdfg").match(Requests.get("/test").withBody("asdfg"))),
        () -> assertTrue(header("header", "value").match(Requests.get("/test").withHeader("header", "value")))
    );
  }

  @Test
  public void bodies() {
    String someJsonObject = """
      {
        "a": 1,
        "b": 2
      }
      """;
    String someJsonArray = """
      [ "a", "b" ]
      """;
    String sameJsonObjectButWithDifferentOrder = """
      {
        "b": 2,
        "a": 1
      }
      """;
    String otherJsonArray = """
      [ "b", "a" ]
      """;
    assertAll(
        () -> assertFalse(json(someJsonArray).match(Requests.get("/test").withBody(otherJsonArray))),
        () -> assertTrue(json(someJsonArray).match(Requests.get("/test").withBody(someJsonArray))),
        () -> assertTrue(json(someJsonObject).match(Requests.get("/test").withBody(sameJsonObjectButWithDifferentOrder))),
        () -> assertTrue(json(someJsonObject).match(Requests.get("/test").withBody(someJsonObject))),
        () -> assertFalse(body(someJsonObject).match(Requests.get("/test").withBody(sameJsonObjectButWithDifferentOrder))),
        () -> assertTrue(body(someJsonObject).match(Requests.get("/test").withBody(someJsonObject)))
    );
  }

  @Test
  public void jsonPath() {
    assertTrue(Matchers.jsonPath("$.a", is("x")).match(Requests.get("/test").withBody("{\"a\": \"x\"}")));
    assertFalse(Matchers.jsonPath("$.a", is("z")).match(Requests.get("/test").withBody("{\"a\": \"x\"}")));
  }
}
