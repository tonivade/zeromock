/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.zeromock.server.ZIOMockHttpServer.builder;

import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.effect.ZIO;
import com.github.tonivade.zeromock.api.ZIORequestHandler;

public class ZIOMockHttpServerRule<R> extends AbstractMockServerRule<Higher1<Higher1<ZIO.µ, R>, Nothing>, ZIORequestHandler<R>> {

  public ZIOMockHttpServerRule(R env, int port) {
    super(builder(Producer.cons(env)).port(port).build());
  }
}
