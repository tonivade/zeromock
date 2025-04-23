/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static com.github.tonivade.zeromock.server.ResponseInterpreterK.async;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.core.Matcher1;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.typeclasses.Instances;
import com.github.tonivade.zeromock.api.AsyncHttpService;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpRouteBuilderK;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PostFilterK;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.api.PreFilterK;
import com.github.tonivade.zeromock.api.RequestHandlerK;
import com.github.tonivade.zeromock.server.MockHttpServerK.BuilderK;

public final class AsyncMockHttpServer implements HttpServer, HttpRouteBuilderK<Future<?>, AsyncMockHttpServer> {

  private final MockHttpServerK<Future<?>> serverK;

  public AsyncMockHttpServer(com.sun.net.httpserver.HttpServer server) {
    this(new MockHttpServerK<>(server, Instances.monad(), async()));
  }

  private AsyncMockHttpServer(MockHttpServerK<Future<?>> serverK) {
    this.serverK = requireNonNull(serverK);
  }

  @Override
  public int getPort() {
    return serverK.getPort();
  }

  @Override
  public String getPath() {
    return serverK.getPath();
  }

  public static BuilderK<Future<?>, AsyncMockHttpServer> builder() {
    return new BuilderK<>(Instances.monad(), async()) {
      @Override
      public AsyncMockHttpServer build() {
        return new AsyncMockHttpServer(buildK());
      }
    };
  }

  public static AsyncMockHttpServer listenAt(int port) {
    return builder().port(port).build();
  }

  public static AsyncMockHttpServer listenAt(String host, int port) {
    return builder().host(host).port(port).build();
  }

  public AsyncMockHttpServer mount(String path, AsyncHttpService other) {
    serverK.mount(path, other.build());
    return this;
  }

  public AsyncMockHttpServer exec(RequestHandlerK<Future<?>> handler) {
    serverK.exec(handler);
    return this;
  }

  public ThenStepK<Future<?>, AsyncMockHttpServer> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serverK.monad(), handler -> addPreFilter(matcher, handler));
  }

  public AsyncMockHttpServer preFilter(PreFilter filter) {
    return preFilter(filter.lift(serverK.monad()));
  }

  public AsyncMockHttpServer preFilter(PreFilterK<Future<?>> filter) {
    serverK.preFilter(filter);
    return this;
  }

  public AsyncMockHttpServer postFilter(PostFilter filter) {
    return postFilter(filter.lift(serverK.monad()));
  }

  public AsyncMockHttpServer postFilter(PostFilterK<Future<?>> filter) {
    serverK.postFilter(filter);
    return this;
  }

  public AsyncMockHttpServer addMapping(Matcher1<HttpRequest> matcher, RequestHandlerK<Future<?>> handler) {
    serverK.addMapping(matcher, handler);
    return this;
  }

  public AsyncMockHttpServer addPreFilter(Matcher1<HttpRequest> matcher, RequestHandlerK<Future<?>> handler) {
    serverK.preFilter(filter(serverK.monad(), matcher, handler));
    return this;
  }

  @Override
  public ThenStepK<Future<?>, AsyncMockHttpServer> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serverK.monad(), handler -> addMapping(matcher, handler));
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
  public AsyncMockHttpServer verify(Matcher1<HttpRequest> matcher, int times) {
    serverK.verify(matcher, times);
    return this;
  }

  @Override
  public AsyncMockHttpServer verifyNot(Matcher1<HttpRequest> matcher) {
    serverK.verifyNot(matcher);
    return this;
  }

  @Override
  public Sequence<HttpRequest> getUnmatched() {
    return serverK.getUnmatched();
  }

  @Override
  public void reset() {
    serverK.reset();
  }
}
