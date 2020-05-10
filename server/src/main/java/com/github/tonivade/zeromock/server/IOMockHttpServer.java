/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.instances.IOInstances;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.zeromock.api.HttpIOService;
import com.github.tonivade.zeromock.api.HttpIOService.MappingBuilder;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.IOPostFilter;
import com.github.tonivade.zeromock.api.IOPreFilter;
import com.github.tonivade.zeromock.api.IORequestHandler;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.server.MockHttpServerK.Builder;

import java.util.List;
import java.util.concurrent.Executor;

import static com.github.tonivade.purefun.instances.FutureInstances.monadDefer;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

public final class IOMockHttpServer implements HttpServer {

  private final MockHttpServerK<IO.µ> serverK;

  private IOMockHttpServer(MockHttpServerK<IO.µ> serverK) {
    this.serverK = requireNonNull(serverK);
  }

  public static Builder<IO.µ> sync() {
    return new Builder<>(IOInstances.monad(), response -> {
      IO<HttpResponse> future = response.fix1(IO::narrowK);
      return Promise.<HttpResponse>make().succeeded(future.unsafeRunSync());
    });
  }

  public static Builder<IO.µ> async() {
    return async(Future.DEFAULT_EXECUTOR);
  }

  public static Builder<IO.µ> async(Executor executor) {
    return new Builder<>(IOInstances.monad(), response -> {
      IO<HttpResponse> effect = response.fix1(IO::narrowK);
      Higher1<Future.µ, HttpResponse> future = effect.foldMap(monadDefer(executor));
      return future.fix1(Future::narrowK).toPromise();
    });
  }

  public static IOMockHttpServer listenAt(int port) {
    return new IOMockHttpServer(sync().port(port).build());
  }

  public IOMockHttpServer mount(String path, HttpIOService other) {
    serverK.mount(path, other.build());
    return this;
  }

  public IOMockHttpServer exec(IORequestHandler handler) {
    serverK.exec(handler);
    return this;
  }

  public MappingBuilder<IOMockHttpServer> preFilter(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addMapping).when(matcher);
  }

  public IOMockHttpServer preFilter(PreFilter filter) {
    return preFilter(filter.andThen(IO::pure)::apply);
  }

  public IOMockHttpServer preFilter(IOPreFilter filter) {
    serverK.preFilter(filter);
    return this;
  }

  public IOMockHttpServer postFilter(PostFilter filter) {
    return postFilter(filter.andThen(IO::pure)::apply);
  }

  public IOMockHttpServer postFilter(IOPostFilter filter) {
    serverK.postFilter(filter);
    return this;
  }

  public IOMockHttpServer addMapping(Matcher1<HttpRequest> matcher, IORequestHandler handler) {
    serverK.addMapping(matcher, handler);
    return this;
  }

  public IOMockHttpServer addPreFilter(Matcher1<HttpRequest> matcher, IORequestHandler handler) {
    serverK.preFilter(filter(IOInstances.monad(), matcher, handler));
    return this;
  }

  public MappingBuilder<IOMockHttpServer> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addMapping).when(matcher);
  }

  @Override
  public IOMockHttpServer start() {
    serverK.start();
    return this;
  }

  @Override
  public void stop() {
    serverK.stop();
  }

  @Override
  public IOMockHttpServer verify(Matcher1<HttpRequest> matcher) {
    serverK.verify(matcher);
    return this;
  }

  @Override
  public IOMockHttpServer verifyNot(Matcher1<HttpRequest> matcher) {
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
