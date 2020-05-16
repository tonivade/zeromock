/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import com.github.tonivade.purefun.type.Id_;

import static com.github.tonivade.zeromock.server.MockHttpServer.builder;

public class SyncMockHttpServerRule extends AbstractMockServerRule<Id_> {

  public SyncMockHttpServerRule(int port) {
    super(builder().port(port).build());
  }
}
