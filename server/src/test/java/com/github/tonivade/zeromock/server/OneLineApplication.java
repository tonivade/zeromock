/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.zeromock.api.Responses;

import static com.github.tonivade.zeromock.api.Handlers.ok;
import static com.github.tonivade.zeromock.api.Headers.contentPlain;
import static com.github.tonivade.zeromock.api.Matchers.get;
import static com.github.tonivade.zeromock.server.MockHttpServer.listenAt;

public class OneLineApplication {

  public static void main(String[] args) {
    listenAt(8080)
        .preFilter(request -> !request.headers().contains("Authorization") ?
            Either.left(Responses.unauthorized()) : Either.right(request))
        .when(get("/ping")).then(ok("pong"))
        .postFilter(contentPlain())
        .start();
  }
}
