/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.zeromock.server.IOMockHttpServer.builder;

import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.typeclasses.Instances;

public class IOMockHttpServerRule extends AbstractMockServerRule<IO<?>> {

  public IOMockHttpServerRule() {
     this(0);
  }

  public IOMockHttpServerRule(int port) {
    super(Instances.monad(), builder().port(port).buildK());
  }
}
