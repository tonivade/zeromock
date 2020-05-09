/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.zeromock.api.AsyncHttpService;
import com.github.tonivade.zeromock.api.AsyncHttpService.MappingBuilder;
import com.github.tonivade.zeromock.api.AsyncPreFilter;
import com.github.tonivade.zeromock.api.AsyncRequestHandler;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.server.MockHttpServerK.Builder;

import java.util.List;

import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

public final class AsyncMockHttpServer implements HttpServer {

  private final MockHttpServerK<Future.µ> serverK;

  private AsyncMockHttpServer(MockHttpServerK<Future.µ> serverK) {
    this.serverK = requireNonNull(serverK);
  }

  public static Builder<Future.µ> builder() {
    return new Builder<>(FutureInstances.monad(), response -> {
      Future<HttpResponse> future = response.fix1(Future::narrowK);
      return future.toPromise();
    });
  }

  public static AsyncMockHttpServer listenAt(int port) {
    return new AsyncMockHttpServer(builder().port(port).build());
  }

  public AsyncMockHttpServer mount(String path, AsyncHttpService other) {
    serverK.mount(path, other.build());
    return this;
  }

  public AsyncMockHttpServer exec(AsyncRequestHandler handler) {
    serverK.exec(handler);
    return this;
  }

  public AsyncMockHttpServer preFilter(AsyncPreFilter filter) {
    serverK.preFilter(filter);
    return this;
  }

  public AsyncMockHttpServer preFilter(Matcher1<HttpRequest> matcher, AsyncRequestHandler handler) {
    serverK.preFilter(filter(FutureInstances.monad(), matcher, handler));
    return this;
  }

  public AsyncMockHttpServer postFilter(PostFilter filter) {
    serverK.postFilter(filter);
    return this;
  }

  public AsyncMockHttpServer add(Matcher1<HttpRequest> matcher, AsyncRequestHandler handler) {
    serverK.add(matcher, handler);
    return this;
  }

  public MappingBuilder<AsyncMockHttpServer> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::add).when(matcher);
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
  public AsyncMockHttpServer verifyNot(Matcher1<HttpRequest> matcher) {
    serverK.verifyNot(matcher);
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
