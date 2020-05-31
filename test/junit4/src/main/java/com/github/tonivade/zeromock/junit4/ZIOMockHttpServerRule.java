/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.purefun.Producer.cons;
import static com.github.tonivade.zeromock.server.ZIOMockHttpServer.sync;
import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.effect.ZIO_;

public class ZIOMockHttpServerRule<R> extends AbstractMockServerRule<Kind<Kind<ZIO_, R>, Nothing>> {

  public ZIOMockHttpServerRule(R env) {
     this(env, 0);
  }

  public ZIOMockHttpServerRule(R env, int port) {
    super(sync(cons(env)).port(port).buildK());
  }
}
