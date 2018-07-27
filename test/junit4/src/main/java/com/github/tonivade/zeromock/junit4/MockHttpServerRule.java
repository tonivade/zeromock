/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import org.junit.rules.ExternalResource;

import com.github.tonivade.purefun.Matcher;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpService;
import com.github.tonivade.zeromock.api.HttpService.MappingBuilder;
import com.github.tonivade.zeromock.api.RequestHandler;
import com.github.tonivade.zeromock.server.MockHttpServer;

public class MockHttpServerRule extends ExternalResource {
  private final MockHttpServer server;

  public MockHttpServerRule(int port) {
    this.server = MockHttpServer.listenAt(port);
  }

  @Override
  protected void before() throws Throwable {
    server.start();
  }

  @Override
  protected void after() {
    server.stop();
  }

  public MockHttpServerRule verify(Matcher<HttpRequest> matcher) {
    server.verify(matcher);
    return this;
  }

  public MockHttpServerRule add(Matcher<HttpRequest> matcher, RequestHandler handler) {
    server.add(matcher, handler);
    return this;
  }

  public MappingBuilder<MockHttpServerRule> when(Matcher<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
  }

  public MockHttpServerRule mount(String path, HttpService service) {
    server.mount(path, service);
    return this;
  }
}
