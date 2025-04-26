/*
 * Copyright (c) 2018-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.purefun.core.Producer.cons;
import static com.github.tonivade.zeromock.server.URIOMockHttpServer.builder;

import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.typeclasses.Instances;

public class URIOMockHttpServerRule<R> extends AbstractMockServerRule<URIO<R, ?>> {

  public URIOMockHttpServerRule(R env) {
     this(env, 0);
  }

  public URIOMockHttpServerRule(R env, int port) {
    super(Instances.<URIO<R, ?>>monad(), builder(cons(env)).port(port).buildK());
  }
}
