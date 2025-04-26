/*
 * Copyright (c) 2018-2025, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static com.github.tonivade.zeromock.server.ResponseInterpreterK.*;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.core.Matcher1;
import com.github.tonivade.purefun.core.Producer;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.effect.URIO;
import com.github.tonivade.purefun.typeclasses.Instances;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpRouteBuilderK;
import com.github.tonivade.zeromock.api.HttpURIOService;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PostFilterK;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.api.PreFilterK;
import com.github.tonivade.zeromock.api.RequestHandlerK;
import com.github.tonivade.zeromock.server.MockHttpServerK.BuilderK;

public final class URIOMockHttpServer<R> implements HttpServer, HttpRouteBuilderK<URIO<R, ?>, URIOMockHttpServer<R>> {

  private final MockHttpServerK<URIO<R, ?>> serverK;

  public URIOMockHttpServer(com.sun.net.httpserver.HttpServer server, Producer<R> env) {
    this(new MockHttpServerK<>(server, Instances.monad(), urio(env)));
  }

  private URIOMockHttpServer(MockHttpServerK<URIO<R, ?>> serverK) {
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

  public static <R> BuilderK<URIO<R, ?>, URIOMockHttpServer<R>> builder(Producer<R> factory) {
    return builder(urio(factory));
  }

  public static <R> URIOMockHttpServer<R> listenAtRandomPort(R env) {
    return listenAt(env, 0);
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

  public URIOMockHttpServer<R> exec(RequestHandlerK<URIO<R, ?>> handler) {
    serverK.exec(handler);
    return this;
  }

  public ThenStepK<URIO<R, ?>, URIOMockHttpServer<R>> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serverK.monad(), handler -> addPreFilter(matcher, handler));
  }

  public URIOMockHttpServer<R> preFilter(PreFilter filter) {
    return preFilter(filter.lift(serverK.monad()));
  }

  public URIOMockHttpServer<R> preFilter(PreFilterK<URIO<R, ?>> filter) {
    serverK.preFilter(filter);
    return this;
  }

  public URIOMockHttpServer<R> postFilter(PostFilter filter) {
    return postFilter(filter.lift(serverK.monad()));
  }

  public URIOMockHttpServer<R> postFilter(PostFilterK<URIO<R, ?>> filter) {
    serverK.postFilter(filter);
    return this;
  }

  public URIOMockHttpServer<R> addMapping(Matcher1<HttpRequest> matcher, RequestHandlerK<URIO<R, ?>> handler) {
    serverK.addMapping(matcher, handler);
    return this;
  }

  public URIOMockHttpServer<R> addPreFilter(Matcher1<HttpRequest> matcher, RequestHandlerK<URIO<R, ?>> handler) {
    serverK.preFilter(filter(serverK.monad(), matcher, handler));
    return this;
  }

  @Override
  public ThenStepK<URIO<R, ?>, URIOMockHttpServer<R>> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serverK.monad(), handler -> addMapping(matcher, handler));
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
  public URIOMockHttpServer<R> verify(Matcher1<HttpRequest> matcher, int times) {
    serverK.verify(matcher, times);
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

  private static <R> BuilderK<URIO<R, ?>, URIOMockHttpServer<R>> builder(
      ResponseInterpreterK<URIO<R, ?>> urioAsync) {
    return new BuilderK<>(Instances.monad(), urioAsync) {
      @Override
      public URIOMockHttpServer<R> build() {
        return new URIOMockHttpServer<>(buildK());
      }
    };
  }
}
