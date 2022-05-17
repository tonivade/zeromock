/*
 * Copyright (c) 2018-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.purefun.typeclasses.Instances.monad;
import static com.github.tonivade.zeromock.server.IOMockHttpServer.builder;
import com.github.tonivade.purefun.monad.IO_;

public class IOMockHttpServerRule extends AbstractMockServerRule<IO_> {

  public IOMockHttpServerRule() {
     this(0);
  }

  public IOMockHttpServerRule(int port) {
    super(monad(IO_.class), builder().port(port).buildK());
  }
}
