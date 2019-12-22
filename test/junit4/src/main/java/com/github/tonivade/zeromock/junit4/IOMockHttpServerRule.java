/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import org.junit.rules.ExternalResource;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.zeromock.api.HttpIOService;
import com.github.tonivade.zeromock.api.HttpIOService.MappingBuilder;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.IORequestHandler;
import com.github.tonivade.zeromock.server.IOMockHttpServer;

public class IOMockHttpServerRule extends ExternalResource {

  private final IOMockHttpServer server;

  public IOMockHttpServerRule(int port) {
    this.server = IOMockHttpServer.listenAt(port);
  }

  @Override
  protected void before() throws Throwable {
    server.start();
  }

  @Override
  protected void after() {
    server.stop();
  }

  public IOMockHttpServerRule verify(Matcher1<HttpRequest> matcher) {
    server.verify(matcher);
    return this;
  }

  public IOMockHttpServerRule add(Matcher1<HttpRequest> matcher, IORequestHandler handler) {
    server.add(matcher, handler);
    return this;
  }

  public MappingBuilder<IOMockHttpServerRule> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  public IOMockHttpServerRule mount(String path, HttpIOService service) {
    server.mount(path, service);
    return this;
  }
}
