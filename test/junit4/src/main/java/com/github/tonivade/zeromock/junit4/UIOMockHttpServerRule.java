/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.zeromock.api.UIORequestHandler;
import com.github.tonivade.zeromock.server.UIOMockHttpServer;

public class UIOMockHttpServerRule extends AbstractMockServerRule<UIO.µ, UIORequestHandler> {

  public UIOMockHttpServerRule(int port) {
    super(UIOMockHttpServer.sync().port(port).build());
  }
}
