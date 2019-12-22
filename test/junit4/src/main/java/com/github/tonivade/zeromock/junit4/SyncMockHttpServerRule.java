/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import static com.github.tonivade.zeromock.server.MockHttpServer.builder;

import com.github.tonivade.purefun.type.Id;

public class SyncMockHttpServerRule extends AbstractMockServerRule<Id.µ> {

  public SyncMockHttpServerRule(int port) {
    super(builder().port(port).build());
  }
}
