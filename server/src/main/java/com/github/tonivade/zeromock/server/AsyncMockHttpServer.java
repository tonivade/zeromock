/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;
import java.util.List;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.FutureOf;
import com.github.tonivade.purefun.concurrent.Future_;
import com.github.tonivade.purefun.instances.FutureInstances;
import com.github.tonivade.zeromock.api.AsyncHttpService;
import com.github.tonivade.zeromock.api.AsyncHttpService.MappingBuilder;
import com.github.tonivade.zeromock.api.AsyncPostFilter;
import com.github.tonivade.zeromock.api.AsyncPreFilter;
import com.github.tonivade.zeromock.api.AsyncRequestHandler;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.server.MockHttpServerK.Builder;

public final class AsyncMockHttpServer implements HttpServer {

  private final MockHttpServerK<Future_> serverK;

  private AsyncMockHttpServer(MockHttpServerK<Future_> serverK) {
    this.serverK = requireNonNull(serverK);
  }

  @Override
  public int getPort() {
    return serverK.getPort();
  }

  public static Builder<Future_> builder() {
    return new Builder<>(FutureInstances.monad(), response -> {
      Future<HttpResponse> future = response.fix(FutureOf::narrowK);
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

  public MappingBuilder<AsyncMockHttpServer> preFilter(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addPreFilter).when(requireNonNull(matcher));
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
    serverK.preFilter(filter(FutureInstances.monad(), matcher, handler));
    return this;
  }

  public MappingBuilder<AsyncMockHttpServer> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addMapping).when(matcher);
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
