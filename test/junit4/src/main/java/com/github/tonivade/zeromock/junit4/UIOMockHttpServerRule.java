/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import org.junit.rules.ExternalResource;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpUIOService;
import com.github.tonivade.zeromock.api.HttpUIOService.MappingBuilder;
import com.github.tonivade.zeromock.api.UIORequestHandler;
import com.github.tonivade.zeromock.server.UIOMockHttpServer;

public class UIOMockHttpServerRule extends ExternalResource {

  private final UIOMockHttpServer server;

  public UIOMockHttpServerRule(int port) {
    this.server = UIOMockHttpServer.listenAt(port);
  }

  @Override
  protected void before() throws Throwable {
    server.start();
  }

  @Override
  protected void after() {
    server.stop();
  }

  public UIOMockHttpServerRule verify(Matcher1<HttpRequest> matcher) {
    server.verify(matcher);
    return this;
  }

  public UIOMockHttpServerRule add(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    server.add(matcher, handler);
    return this;
  }

  public MappingBuilder<UIOMockHttpServerRule> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  public UIOMockHttpServerRule mount(String path, HttpUIOService service) {
    server.mount(path, service);
    return this;
  }
}
