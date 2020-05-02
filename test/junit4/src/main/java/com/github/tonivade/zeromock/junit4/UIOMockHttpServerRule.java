/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.zeromock.server.UIOMockHttpServer.sync;

import com.github.tonivade.purefun.effect.UIO;

public class UIOMockHttpServerRule extends AbstractMockServerRule<UIO.µ> {

  public UIOMockHttpServerRule(int port) {
    super(sync().port(port).build());
  }
}
