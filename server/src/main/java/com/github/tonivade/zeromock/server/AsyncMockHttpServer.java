/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.typeclasses.Instance.monad;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static com.github.tonivade.zeromock.server.ResponseInterpreterK.async;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.Future_;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.typeclasses.Instance;
import com.github.tonivade.zeromock.api.AsyncHttpService;
import com.github.tonivade.zeromock.api.AsyncPostFilter;
import com.github.tonivade.zeromock.api.AsyncPreFilter;
import com.github.tonivade.zeromock.api.AsyncRequestHandler;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpRouteBuilderK;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.server.MockHttpServerK.BuilderK;

public final class AsyncMockHttpServer implements HttpServer, HttpRouteBuilderK<Future_, AsyncMockHttpServer> {

  private final MockHttpServerK<Future_> serverK;

  public AsyncMockHttpServer(com.sun.net.httpserver.HttpServer server) {
    this(new MockHttpServerK<>(server, monad(Future_.class), async()));
  }

  private AsyncMockHttpServer(MockHttpServerK<Future_> serverK) {
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

  public static BuilderK<Future_, AsyncMockHttpServer> builder() {
    return new BuilderK<>(monad(Future_.class), async()) {
      @Override
      public AsyncMockHttpServer build() {
        return new AsyncMockHttpServer(buildK());
      }
    };
  }

  public static AsyncMockHttpServer listenAt(int port) {
    return builder().port(port).build();
  }

  public AsyncMockHttpServer mount(String path, AsyncHttpService other) {
    serverK.mount(path, other.build());
    return this;
  }

  public AsyncMockHttpServer exec(AsyncRequestHandler handler) {
    serverK.exec(handler);
    return this;
  }

  public ThenStepK<Future_, AsyncMockHttpServer> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(Instance.monad(Future_.class), handler -> addPreFilter(matcher, handler::apply));
  }

  public AsyncMockHttpServer preFilter(PreFilter filter) {
    return preFilter(filter.andThen(Future::success)::apply);
  }

  public AsyncMockHttpServer preFilter(AsyncPreFilter filter) {
    serverK.preFilter(filter);
    return this;
  }

  public AsyncMockHttpServer postFilter(PostFilter filter) {
    return postFilter(filter.andThen(Future::success)::apply);
  }

  public AsyncMockHttpServer postFilter(AsyncPostFilter filter) {
    serverK.postFilter(filter);
    return this;
  }

  public AsyncMockHttpServer addMapping(Matcher1<HttpRequest> matcher, AsyncRequestHandler handler) {
    serverK.addMapping(matcher, handler);
    return this;
  }

  public AsyncMockHttpServer addPreFilter(Matcher1<HttpRequest> matcher, AsyncRequestHandler handler) {
    serverK.preFilter(filter(monad(Future_.class), matcher, handler));
    return this;
  }

  @Override
  public ThenStepK<Future_, AsyncMockHttpServer> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(Instance.monad(Future_.class), handler -> addMapping(matcher, handler::apply));
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
  public Sequence<HttpRequest> getUnmatched() {
    return serverK.getUnmatched();
  }

  @Override
  public void reset() {
    serverK.reset();
  }
}
