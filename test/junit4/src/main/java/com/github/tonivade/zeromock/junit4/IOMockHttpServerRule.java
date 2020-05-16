/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import com.github.tonivade.purefun.monad.IO_;

import static com.github.tonivade.zeromock.server.IOMockHttpServer.sync;

public class IOMockHttpServerRule extends AbstractMockServerRule<IO_> {

  public IOMockHttpServerRule(int port) {
    super(sync().port(port).build());
  }
}
