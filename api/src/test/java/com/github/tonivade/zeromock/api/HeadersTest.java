/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.api;

import static com.github.tonivade.purefun.data.Sequence.setOf;
import static com.github.tonivade.zeromock.api.Headers.contentJson;
import static com.github.tonivade.zeromock.api.Headers.contentPlain;
import static com.github.tonivade.zeromock.api.Headers.contentXml;
import static com.github.tonivade.zeromock.api.Responses.ok;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HeadersTest {

  @Test
  public void headers() {
    assertAll(
        () -> assertEquals(setOf("application/json"), contentJson().apply(ok()).headers().get("Content-type")),
        () -> assertEquals(setOf("text/xml"), contentXml().apply(ok()).headers().get("Content-type")),
        () -> assertEquals(setOf("text/plain"), contentPlain().apply(ok()).headers().get("Content-type"))
        );
  }
}
