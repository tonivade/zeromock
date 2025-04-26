/*
 * Copyright (c) 2018-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.zeromock.server.UIOMockHttpServer.builder;

import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.typeclasses.Instances;

public class UIOMockHttpServerRule extends AbstractMockServerRule<UIO<?>> {

  public UIOMockHttpServerRule() {
     this(0);
  }

  public UIOMockHttpServerRule(int port) {
    super(Instances.monad(), builder().port(port).buildK());
  }
}
