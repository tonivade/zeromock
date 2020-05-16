/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.effect.ZIO_;

import static com.github.tonivade.purefun.Producer.cons;
import static com.github.tonivade.zeromock.server.ZIOMockHttpServer.builder;

public class ZIOMockHttpServerRule<R> extends AbstractMockServerRule<Higher1<Higher1<ZIO_, R>, Nothing>> {

  public ZIOMockHttpServerRule(R env, int port) {
    super(builder(cons(env)).port(port).build());
  }
}
