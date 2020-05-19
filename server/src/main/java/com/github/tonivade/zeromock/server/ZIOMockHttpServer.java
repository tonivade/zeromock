/*
 * Copyright (c) 2018-2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import com.github.tonivade.purefun.Higher1;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Nothing;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.concurrent.Future;
import com.github.tonivade.purefun.concurrent.FutureOf;
import com.github.tonivade.purefun.concurrent.Future_;
import com.github.tonivade.purefun.concurrent.Promise;
import com.github.tonivade.purefun.effect.ZIO;
import com.github.tonivade.purefun.effect.ZIOOf;
import com.github.tonivade.purefun.effect.ZIO_;
import com.github.tonivade.purefun.instances.ZIOInstances;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpZIOService;
import com.github.tonivade.zeromock.api.HttpZIOService.MappingBuilder;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.api.ZIOPostFilter;
import com.github.tonivade.zeromock.api.ZIOPreFilter;
import com.github.tonivade.zeromock.api.ZIORequestHandler;
import com.github.tonivade.zeromock.server.MockHttpServerK.Builder;

import java.util.List;
import java.util.concurrent.Executor;

import static com.github.tonivade.purefun.instances.FutureInstances.monadDefer;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

public final class ZIOMockHttpServer<R> implements HttpServer {

  private final MockHttpServerK<Higher1<Higher1<ZIO_, R>, Nothing>> serverK;

  private ZIOMockHttpServer(MockHttpServerK<Higher1<Higher1<ZIO_, R>, Nothing>> serverK) {
    this.serverK = requireNonNull(serverK);
  }

  public static <R> Builder<Higher1<Higher1<ZIO_, R>, Nothing>> builder(Producer<R> factory) {
    return new Builder<>(ZIOInstances.monad(), response -> {
      ZIO<R, Nothing, HttpResponse> future = response.fix1(ZIOOf::narrowK);
      return Promise.<HttpResponse>make().succeeded(future.provide(factory.get()).get());
    });
  }

  public static <R> Builder<Higher1<Higher1<ZIO_, R>, Nothing>> async(Producer<R> factory) {
    return async(Future.DEFAULT_EXECUTOR, factory);
  }

  public static <R> Builder<Higher1<Higher1<ZIO_, R>, Nothing>> async(Executor executor, Producer<R> factory) {
    return new Builder<>(ZIOInstances.monad(), response -> {
      ZIO<R, Nothing, HttpResponse> effect = response.fix1(ZIOOf::narrowK);
      Higher1<Future_, Either<Nothing, HttpResponse>> future = effect.foldMap(factory.get(), monadDefer(executor));
      return future.fix1(FutureOf::narrowK).map(Either::get).toPromise();
    });
  }

  public static <R> ZIOMockHttpServer<R> listenAt(R env, int port) {
    return new ZIOMockHttpServer<>(builder(Producer.cons(env)).port(port).build());
  }

  public ZIOMockHttpServer<R> mount(String path, HttpZIOService<R> other) {
    serverK.mount(path, other.build());
    return this;
  }

  public ZIOMockHttpServer<R> exec(ZIORequestHandler<R> handler) {
    serverK.exec(handler);
    return this;
  }

  public MappingBuilder<R, ZIOMockHttpServer<R>> preFilter(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addMapping).when(matcher);
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

  public MappingBuilder<R, ZIOMockHttpServer<R>> when(Matcher1<HttpRequest> matcher) {
    return new MappingBuilder<>(this::addMapping).when(matcher);
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
  public List<HttpRequest> getUnmatched() {
    return serverK.getUnmatched();
  }

  @Override
  public void reset() {
    serverK.reset();
  }
}
