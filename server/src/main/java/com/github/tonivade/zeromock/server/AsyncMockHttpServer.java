/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.zeromock.server.MockHttpServerK.async;

import java.util.List;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.zeromock.api.AsyncHttpService;
import com.github.tonivade.zeromock.api.AsyncHttpService.AsyncMappingBuilder;
import com.github.tonivade.zeromock.api.AsyncRequestHandler;
import com.github.tonivade.zeromock.api.HttpRequest;

public final class AsyncMockHttpServer {

  private MockHttpServerK<Future.µ> serverK;

  private AsyncMockHttpServer(String host, int port, int threads, int backlog) {
    this.serverK = async().host(host).port(port).threads(threads).backlog(backlog).build();
  }

  private AsyncMockHttpServer(MockHttpServerK<Future.µ> serverK) {
    this.serverK = serverK;
  }

  public static AsyncMockHttpServer listenAt(int port) {
    return new AsyncMockHttpServer(async().port(port).build());
  }

  public AsyncMockHttpServer mount(String path, AsyncHttpService other) {
    serverK.mount(path, other.serviceK());
    return this;
  }

  public AsyncMockHttpServer exec(AsyncRequestHandler handler) {
    serverK.exec(handler);
    return this;
  }

  public AsyncMockHttpServer add(Matcher1<HttpRequest> matcher, AsyncRequestHandler handler) {
    serverK.add(matcher, handler);
    return this;
  }

  public AsyncMappingBuilder<AsyncMockHttpServer> when(Matcher1<HttpRequest> matcher) {
    return new AsyncMappingBuilder<>(this::add).when(matcher);
  }

  public AsyncMockHttpServer start() {
    serverK.start();
    return this;
  }

  public void stop() {
    serverK.stop();
  }

  public AsyncMockHttpServer verify(Matcher1<HttpRequest> matcher) {
    serverK.verify(matcher);
    return this;
  }

  public List<HttpRequest> getUnmatched() {
    return serverK.getUnmatched();
  }

  public void reset() {
    serverK.reset();
  }
}
