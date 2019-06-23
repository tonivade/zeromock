/*
 * Copyright (c) 2018-2019, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static java.util.Objects.requireNonNull;

import java.util.List;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.zeromock.api.AsyncHttpService;
import com.github.tonivade.zeromock.api.AsyncHttpService.AsyncMappingBuilder;
import com.github.tonivade.zeromock.api.AsyncRequestHandler;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.server.MockHttpServerK.Builder;

public final class AsyncMockHttpServer implements HttpServer {

  private MockHttpServerK<Future.µ> serverK;

  private AsyncMockHttpServer(MockHttpServerK<Future.µ> serverK) {
    this.serverK = requireNonNull(serverK);
  }

  public static Builder<Future.µ> builder() {
    return new Builder<>(response -> {
      Future<HttpResponse> future = response.fix1(Future::narrowK);
      return future.toPromise();
    });
  }

  public static AsyncMockHttpServer listenAt(int port) {
    return new AsyncMockHttpServer(builder().port(port).build());
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

  @Override
  public AsyncMockHttpServer start() {
    serverK.start();
    return this;
  }

  @Override
  public void stop() {
    serverK.stop();
  }

  @Override
  public AsyncMockHttpServer verify(Matcher1<HttpRequest> matcher) {
    serverK.verify(matcher);
    return this;
  }

  @Override
  public List<HttpRequest> getUnmatched() {
    return serverK.getUnmatched();
  }

  @Override
  public void reset() {
    serverK.reset();
  }
}
