/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Requests.get;
import static com.github.tonivade.zeromock.api.Responses.badRequest;
import static com.github.tonivade.zeromock.api.Responses.created;
import static com.github.tonivade.zeromock.api.Responses.error;
import static com.github.tonivade.zeromock.api.Responses.forbidden;
import static com.github.tonivade.zeromock.api.Responses.noContent;
import static com.github.tonivade.zeromock.api.Responses.notFound;
import static com.github.tonivade.zeromock.api.Responses.ok;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HandlersTest {

  @Test
  public void handlers() {
    assertAll(
        () -> assertEquals(badRequest(), Handlers.badRequest().apply(get("/whatever"))),
        () -> assertEquals(error(), Handlers.error().apply(get("/whatever"))),
        () -> assertEquals(forbidden(), Handlers.forbidden().apply(get("/whatever"))),
        () -> assertEquals(noContent(), Handlers.noContent().apply(get("/whatever"))),
        () -> assertEquals(notFound(), Handlers.notFound().apply(get("/whatever"))),
        () -> assertEquals(ok(), Handlers.ok().apply(get("/whatever"))),
        () -> assertEquals(badRequest("error"), Handlers.badRequest("error").apply(get("/whatever"))),
        () -> assertEquals(error("error"), Handlers.error("error").apply(get("/whatever"))),
        () -> assertEquals(created("error"), Handlers.created("error").apply(get("/whatever"))),
        () -> assertEquals(notFound("error"), Handlers.notFound("error").apply(get("/whatever"))),
        () -> assertEquals(ok("error"), Handlers.ok("error").apply(get("/whatever")))
        );
  }
}
