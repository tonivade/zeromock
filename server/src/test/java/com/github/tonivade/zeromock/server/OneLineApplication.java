/*
 * Copyright (c) 2018-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.zeromock.api.Handlers.ok;
import static com.github.tonivade.zeromock.api.Handlers.unauthorized;
import static com.github.tonivade.zeromock.api.Headers.contentPlain;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.api.Matchers.header;
import static com.github.tonivade.zeromock.server.MockHttpServer.listenAt;

public class OneLineApplication {

  public static void main(String[] args) {
    listenAt(8080)
        .preFilter(header("Authorization").negate()).then(unauthorized())
        .when(get("/ping")).then(ok("pong"))
        .postFilter(contentPlain())
        .start();
  }
}
