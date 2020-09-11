/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.concurrent.Future.DEFAULT_EXECUTOR;
import static com.github.tonivade.purefun.instances.ZIOInstances.monad;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static com.github.tonivade.zeromock.server.ResponseInterpreterK.zioAsync;
import static com.github.tonivade.zeromock.server.ResponseInterpreterK.zioSync;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.effect.ZIO;
import com.github.tonivade.purefun.effect.ZIO_;
import com.github.tonivade.purefun.instances.ZIOInstances;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpZIOService;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.api.ZIOPostFilter;
import com.github.tonivade.zeromock.api.ZIOPreFilter;
import com.github.tonivade.zeromock.api.ZIORequestHandler;
import com.github.tonivade.zeromock.server.MockHttpServerK.BuilderK;

public final class ZIOMockHttpServer<R> implements HttpServer {

  private final MockHttpServerK<Kind<Kind<ZIO_, R>, Nothing>> serverK;

  private ZIOMockHttpServer(MockHttpServerK<Kind<Kind<ZIO_, R>, Nothing>> serverK) {
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

  public static <R> BuilderK<Kind<Kind<ZIO_, R>, Nothing>, ZIOMockHttpServer<R>> sync(Producer<R> factory) {
    return _builder(zioSync(factory));
  }

  public static <R> BuilderK<Kind<Kind<ZIO_, R>, Nothing>, ZIOMockHttpServer<R>> async(Producer<R> factory) {
    return async(DEFAULT_EXECUTOR, factory);
  }

  public static <R> BuilderK<Kind<Kind<ZIO_, R>, Nothing>, ZIOMockHttpServer<R>> async(Executor executor, Producer<R> factory) {
    return _builder(zioAsync(factory, executor));
  }

  public static <R> ZIOMockHttpServer<R> listenAt(R env, int port) {
    return sync(Producer.cons(env)).port(port).build();
  }

  public ZIOMockHttpServer<R> mount(String path, HttpZIOService<R> other) {
    serverK.mount(path, other.build());
    return this;
  }

  public ZIOMockHttpServer<R> exec(ZIORequestHandler<R> handler) {
    serverK.exec(handler);
    return this;
  }

  public HttpZIOService.ThenStep<R, ZIOMockHttpServer<R>> preFilter(Matcher1<HttpRequest> matcher) {
    return handler -> addPreFilter(matcher, handler);
  }

  public ZIOMockHttpServer<R> preFilter(PreFilter filter) {
    return preFilter(filter.andThen(ZIO::<R, Nothing, Either<HttpResponse, HttpRequest>>pure)::apply);
  }

  public ZIOMockHttpServer<R> preFilter(ZIOPreFilter<R> filter) {
    serverK.preFilter(filter);
    return this;
  }

  public ZIOMockHttpServer<R> postFilter(PostFilter filter) {
    return postFilter(filter.andThen(ZIO::<R, Nothing, HttpResponse>pure)::apply);
  }

  public ZIOMockHttpServer<R> postFilter(ZIOPostFilter<R> filter) {
    serverK.postFilter(filter);
    return this;
  }

  public ZIOMockHttpServer<R> addMapping(Matcher1<HttpRequest> matcher, ZIORequestHandler<R> handler) {
    serverK.addMapping(matcher, handler);
    return this;
  }

  public ZIOMockHttpServer<R> addPreFilter(Matcher1<HttpRequest> matcher, ZIORequestHandler<R> handler) {
    serverK.preFilter(filter(ZIOInstances.monad(), matcher, handler));
    return this;
  }

  public HttpZIOService.ThenStep<R, ZIOMockHttpServer<R>> when(Matcher1<HttpRequest> matcher) {
    return handler -> addMapping(matcher, handler);
  }

  @Override
  public ZIOMockHttpServer<R> start() {
    serverK.start();
    return this;
  }

  @Override
  public void stop() {
    serverK.stop();
  }

  @Override
  public ZIOMockHttpServer<R> verify(Matcher1<HttpRequest> matcher) {
    serverK.verify(matcher);
    return this;
  }

  @Override
  public ZIOMockHttpServer<R> verifyNot(Matcher1<HttpRequest> matcher) {
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

  private static <R> BuilderK<Kind<Kind<ZIO_, R>, Nothing>, ZIOMockHttpServer<R>> _builder(
      ResponseInterpreterK<Kind<Kind<ZIO_, R>, Nothing>> zioAsync) {
    return new BuilderK<Kind<Kind<ZIO_, R>, Nothing>, ZIOMockHttpServer<R>>(monad(), zioAsync) {
      @Override
      public ZIOMockHttpServer<R> build() {
        return new ZIOMockHttpServer<>(buildK());
      }
    };
  }
}
