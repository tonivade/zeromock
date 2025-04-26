/*
 * Copyright (c) 2018-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.zeromock.server.AsyncMockHttpServer.builder;

import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.typeclasses.Instances;

public class AsyncMockHttpServerRule extends AbstractMockServerRule<Future<?>> {

  public AsyncMockHttpServerRule() {
     this(0);
  }

  public AsyncMockHttpServerRule(int port) {
    super(Instances.monad(), builder().port(port).buildK());
  }
}
