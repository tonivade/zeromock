/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.junit4;

import org.junit.rules.ExternalResource;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.zeromock.api.AsyncHttpService;
import com.github.tonivade.zeromock.api.AsyncHttpService.AsyncMappingBuilder;
import com.github.tonivade.zeromock.api.AsyncRequestHandler;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.server.AsyncMockHttpServer;

public class AsyncMockHttpServerRule extends ExternalResource {

  private final AsyncMockHttpServer server;

  public AsyncMockHttpServerRule(int port) {
    this.server = AsyncMockHttpServer.listenAt(port);
  }

  @Override
  protected void before() throws Throwable {
    server.start();
  }

  @Override
  protected void after() {
    server.stop();
  }

  public AsyncMockHttpServerRule verify(Matcher1<HttpRequest> matcher) {
    server.verify(matcher);
    return this;
  }

  public AsyncMockHttpServerRule add(Matcher1<HttpRequest> matcher, AsyncRequestHandler handler) {
    server.add(matcher, handler);
    return this;
  }

  public AsyncMappingBuilder<AsyncMockHttpServerRule> when(Matcher1<HttpRequest> matcher) {
    return new AsyncMappingBuilder<>(this::add).when(matcher);
  }

  public AsyncMockHttpServerRule mount(String path, AsyncHttpService service) {
    server.mount(path, service);
    return this;
  }
}
