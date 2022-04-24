/*
 * Copyright (c) 2018-2022, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.instances.URIOInstances.monad;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static com.github.tonivade.zeromock.server.ResponseInterpreterK.urio;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.Kind;
import com.github.tonivade.purefun.Matcher1;
import com.github.tonivade.purefun.Producer;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.effect.URIO_;
import com.github.tonivade.purefun.instances.URIOInstances;
import com.github.tonivade.purefun.type.Either;
import com.github.tonivade.purefun.typeclasses.Instance;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpResponse;
import com.github.tonivade.zeromock.api.HttpRouteBuilderK;
import com.github.tonivade.zeromock.api.HttpURIOService;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.api.URIOPostFilter;
import com.github.tonivade.zeromock.api.URIOPreFilter;
import com.github.tonivade.zeromock.api.URIORequestHandler;
import com.github.tonivade.zeromock.server.MockHttpServerK.BuilderK;

public final class URIOMockHttpServer<R> implements HttpServer, HttpRouteBuilderK<Kind<URIO_, R>, URIOMockHttpServer<R>> {

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

  public static <R> BuilderK<Kind<URIO_, R>, URIOMockHttpServer<R>> builder(Producer<R> factory) {
    return builder(urio(factory));
  }

  public static <R> URIOMockHttpServer<R> listenAt(R env, int port) {
    return builder(Producer.cons(env)).port(port).build();
  }

  public static <R> URIOMockHttpServer<R> listenAt(R env, String host, int port) {
    return builder(Producer.cons(env)).host(host).port(port).build();
  }

  public URIOMockHttpServer<R> mount(String path, HttpURIOService<R> other) {
    serverK.mount(path, other.build());
    return this;
  }

  public URIOMockHttpServer<R> exec(URIORequestHandler<R> handler) {
    serverK.exec(handler);
    return this;
  }

  public ThenStepK<Kind<URIO_, R>, URIOMockHttpServer<R>> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(new Instance<Kind<URIO_, R>>() {}.monad(), handler -> addPreFilter(matcher, handler::apply));
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

  public ThenStepK<Kind<URIO_, R>, URIOMockHttpServer<R>> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(new Instance<Kind<URIO_, R>>() {}.monad(), handler -> addMapping(matcher, handler::apply));
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

  private static <R> BuilderK<Kind<URIO_, R>, URIOMockHttpServer<R>> builder(
      ResponseInterpreterK<Kind<URIO_, R>> urioAsync) {
    return new BuilderK<>(monad(), urioAsync) {
      @Override
      public URIOMockHttpServer<R> build() {
        return new URIOMockHttpServer<>(buildK());
      }
    };
  }
}
