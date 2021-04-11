/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.zeromock.server.AsyncMockHttpServer.builder;

import com.github.tonivade.purefun.concurrent.Future_;

public class AsyncMockHttpServerRule extends AbstractMockServerRule<Future_> {

  public AsyncMockHttpServerRule() {
     this(0);
  }

  public AsyncMockHttpServerRule(int port) {
    super(builder().port(port).buildK());
  }
}
