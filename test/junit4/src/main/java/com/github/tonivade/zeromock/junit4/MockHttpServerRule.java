/*
 * Copyright (c) 2018, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.rules.ExternalResource;

import com.github.tonivade.zeromock.core.HttpRequest;
import com.github.tonivade.zeromock.core.HttpResponse;
import com.github.tonivade.zeromock.core.HttpService;
import com.github.tonivade.zeromock.core.Mappings.Mapping;
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

  public MockHttpServerRule verify(Predicate<HttpRequest> matcher) {
    server.verify(matcher);
    return this;
  }

  public MockHttpServerRule when(Predicate<HttpRequest> matcher, 
                                 Function<HttpRequest, HttpResponse> handler) {
    server.when(matcher, handler);
    return this;
  }

  public MockHttpServerRule when(Mapping mapping) {
    server.when(mapping);
    return this;
  }

  public MockHttpServerRule mount(String path, HttpService service) {
    server.mount(path, service);
    return this;
  }
}
