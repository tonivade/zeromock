/*
 * Copyright (c) 2018-2021, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.concurrent.Future.DEFAULT_EXECUTOR;
import static com.github.tonivade.purefun.instances.URIOInstances.monad;
import static com.github.tonivade.purefun.instances.URIOInstances.runtime;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executor;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.effect.URIO_;
import com.github.tonivade.purefun.instances.URIOInstances;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpURIOService;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.api.URIOPostFilter;
import com.github.tonivade.zeromock.api.URIOPreFilter;
import com.github.tonivade.zeromock.api.URIORequestHandler;
import com.github.tonivade.zeromock.server.MockHttpServerK.BuilderK;

public final class URIOMockHttpServer<R> implements HttpServer {

  private final MockHttpServerK<Kind<URIO_, R>> serverK;

  private URIOMockHttpServer(MockHttpServerK<Kind<URIO_, R>> serverK) {
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

  public static <R> BuilderK<Kind<URIO_, R>, URIOMockHttpServer<R>> sync(Producer<R> factory) {
    return _builder(ResponseInterpreterK.sync(runtime(factory.get())));
  }

  public static <R> BuilderK<Kind<URIO_, R>, URIOMockHttpServer<R>> async(Producer<R> factory) {
    return async(DEFAULT_EXECUTOR, factory);
  }

  public static <R> BuilderK<Kind<URIO_, R>, URIOMockHttpServer<R>> async(Executor executor, Producer<R> factory) {
    return _builder(ResponseInterpreterK.async(runtime(factory.get()), executor));
  }

  public static <R> URIOMockHttpServer<R> listenAt(R env, int port) {
    return sync(Producer.cons(env)).port(port).build();
  }

  public URIOMockHttpServer<R> mount(String path, HttpURIOService<R> other) {
    serverK.mount(path, other.build());
    return this;
  }

  public URIOMockHttpServer<R> exec(URIORequestHandler<R> handler) {
    serverK.exec(handler);
    return this;
  }

  public HttpURIOService.ThenStep<R, URIOMockHttpServer<R>> preFilter(Matcher1<HttpRequest> matcher) {
    return handler -> addPreFilter(matcher, handler);
  }

  public URIOMockHttpServer<R> preFilter(PreFilter filter) {
    return preFilter(filter.andThen(URIO::<R, Either<HttpResponse, HttpRequest>>pure)::apply);
  }

  public URIOMockHttpServer<R> preFilter(URIOPreFilter<R> filter) {
    serverK.preFilter(filter);
    return this;
  }

  public URIOMockHttpServer<R> postFilter(PostFilter filter) {
    return postFilter(filter.andThen(URIO::<R, HttpResponse>pure)::apply);
  }

  public URIOMockHttpServer<R> postFilter(URIOPostFilter<R> filter) {
    serverK.postFilter(filter);
    return this;
  }

  public URIOMockHttpServer<R> addMapping(Matcher1<HttpRequest> matcher, URIORequestHandler<R> handler) {
    serverK.addMapping(matcher, handler);
    return this;
  }

  public URIOMockHttpServer<R> addPreFilter(Matcher1<HttpRequest> matcher, URIORequestHandler<R> handler) {
    serverK.preFilter(filter(URIOInstances.monad(), matcher, handler));
    return this;
  }

  public HttpURIOService.ThenStep<R, URIOMockHttpServer<R>> when(Matcher1<HttpRequest> matcher) {
    return handler -> addMapping(matcher, handler);
  }

  @Override
  public URIOMockHttpServer<R> start() {
    serverK.start();
    return this;
  }

  @Override
  public void stop() {
    serverK.stop();
  }

  @Override
  public URIOMockHttpServer<R> verify(Matcher1<HttpRequest> matcher) {
    serverK.verify(matcher);
    return this;
  }

  @Override
  public URIOMockHttpServer<R> verifyNot(Matcher1<HttpRequest> matcher) {
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

  private static <R> BuilderK<Kind<URIO_, R>, URIOMockHttpServer<R>> _builder(
      ResponseInterpreterK<Kind<URIO_, R>> urioAsync) {
    return new BuilderK<>(monad(), urioAsync) {
      @Override
      public URIOMockHttpServer<R> build() {
        return new URIOMockHttpServer<>(buildK());
      }
    };
  }
}
