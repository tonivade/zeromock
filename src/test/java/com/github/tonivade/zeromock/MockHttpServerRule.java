/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock;

import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.rules.ExternalResource;

import com.github.tonivade.zeromock.HttpRequest;
import com.github.tonivade.zeromock.HttpResponse;
import com.github.tonivade.zeromock.HttpService;
import com.github.tonivade.zeromock.MockHttpServer;

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

  public MockHttpServerRule verify(Predicate<HttpRequest> matcher) {
    server.verify(matcher);
    return this;
  }

  public MockHttpServerRule when(Predicate<HttpRequest> matcher, 
                                 Function<HttpRequest, HttpResponse> handler) {
    server.when(matcher, handler);
    return this;
  }

  public MockHttpServerRule mount(String path, HttpService service) {
    server.mount(path, service);
    return this;
  }
}
