/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.purefun.core.Producer.cons;
import static com.github.tonivade.zeromock.server.URIOMockHttpServer.builder;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.typeclasses.Instance;

public class URIOMockHttpServerRule<R> extends AbstractMockServerRule<Kind<URIO<?, ?>, R>> {

  public URIOMockHttpServerRule(R env) {
     this(env, 0);
  }

  public URIOMockHttpServerRule(R env, int port) {
    super(new Instance<Kind<URIO<?, ?>, R>>() {}.monad(), builder(cons(env)).port(port).buildK());
  }
}
