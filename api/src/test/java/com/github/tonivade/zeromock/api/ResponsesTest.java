/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.zeromock.api.Bytes.asBytes;
import static com.github.tonivade.zeromock.api.Bytes.empty;
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

public class ResponsesTest {

  @Test
  public void status() {
    assertAll(
        () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error().status()),
        () -> assertEquals(HttpStatus.BAD_REQUEST, badRequest().status()),
        () -> assertEquals(HttpStatus.NO_CONTENT, noContent().status()),
        () -> assertEquals(HttpStatus.NOT_FOUND, notFound().status()),
        () -> assertEquals(HttpStatus.FORBIDDEN, forbidden().status()),
        () -> assertEquals(HttpStatus.OK, ok().status()),
        () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error("body").status()),
        () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error(new Exception("error")).status()),
        () -> assertEquals(HttpStatus.CREATED, created("body").status()),
        () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error("body").status()),
        () -> assertEquals(HttpStatus.BAD_REQUEST, badRequest("body").status()),
        () -> assertEquals(HttpStatus.NOT_FOUND, notFound("body").status()),
        () -> assertEquals(HttpStatus.OK, ok("body").status())
        );
  }

  @Test
  public void body() {
    assertAll(
        () -> assertEquals(empty(), error().body()),
        () -> assertEquals(empty(), badRequest().body()),
        () -> assertEquals(empty(), noContent().body()),
        () -> assertEquals(empty(), notFound().body()),
        () -> assertEquals(empty(), forbidden().body()),
        () -> assertEquals(empty(), ok().body()),
        () -> assertEquals(asBytes("body"), error("body").body()),
        () -> assertEquals(asBytes("error"), error(new Exception("error")).body()),
        () -> assertEquals(asBytes("body"), created("body").body()),
        () -> assertEquals(asBytes("body"), error("body").body()),
        () -> assertEquals(asBytes("body"), badRequest("body").body()),
        () -> assertEquals(asBytes("body"), notFound("body").body()),
        () -> assertEquals(asBytes("body"), ok("body").body())
        );
  }
}
