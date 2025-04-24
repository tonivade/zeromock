/*
 * Copyright (c) 2018-2024, Antonio Gabriel Muñoz Conejo <me at tonivade dot es>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static com.github.tonivade.zeromock.server.ResponseInterpreterK.io;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.core.Matcher1;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.monad.IO;
import com.github.tonivade.purefun.typeclasses.Instances;
import com.github.tonivade.zeromock.api.HttpIOService;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpRouteBuilderK;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PostFilterK;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.api.PreFilterK;
import com.github.tonivade.zeromock.api.RequestHandlerK;
import com.github.tonivade.zeromock.server.MockHttpServerK.BuilderK;

public final class IOMockHttpServer implements HttpServer, HttpRouteBuilderK<IO<?>, IOMockHttpServer> {

  private final MockHttpServerK<IO<?>> serverK;

  public IOMockHttpServer(com.sun.net.httpserver.HttpServer server) {
    this(new MockHttpServerK<>(server, Instances.monad(), io()));
  }

  private IOMockHttpServer(MockHttpServerK<IO<?>> serverK) {
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

  public static BuilderK<IO<?>, IOMockHttpServer> builder() {
    return builder(io());
  }

  public static IOMockHttpServer listenAtRandomPort() {
    return listenAt(0);
  }

  public static IOMockHttpServer listenAt(int port) {
    return builder().port(port).build();
  }

  public static IOMockHttpServer listenAt(String host, int port) {
    return builder().host(host).port(port).build();
  }

  public IOMockHttpServer mount(String path, HttpIOService other) {
    serverK.mount(path, other.build());
    return this;
  }

  public IOMockHttpServer exec(RequestHandlerK<IO<?>> handler) {
    serverK.exec(handler);
    return this;
  }

  public ThenStepK<IO<?>, IOMockHttpServer> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serverK.monad(), handler -> addPreFilter(matcher, handler));
  }

  public IOMockHttpServer preFilter(PreFilter filter) {
    return preFilter(filter.lift(serverK.monad()));
  }

  public IOMockHttpServer preFilter(PreFilterK<IO<?>> filter) {
    serverK.preFilter(filter);
    return this;
  }

  public IOMockHttpServer postFilter(PostFilter filter) {
    return postFilter(filter.lift(serverK.monad()));
  }

  public IOMockHttpServer postFilter(PostFilterK<IO<?>> filter) {
    serverK.postFilter(filter);
    return this;
  }

  public IOMockHttpServer addMapping(Matcher1<HttpRequest> matcher, RequestHandlerK<IO<?>> handler) {
    serverK.addMapping(matcher, handler);
    return this;
  }

  public IOMockHttpServer addPreFilter(Matcher1<HttpRequest> matcher, RequestHandlerK<IO<?>> handler) {
    serverK.preFilter(filter(serverK.monad(), matcher, handler));
    return this;
  }

  @Override
  public ThenStepK<IO<?>, IOMockHttpServer> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(serverK.monad(), handler -> addMapping(matcher, handler));
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
  public IOMockHttpServer verify(Matcher1<HttpRequest> matcher, int times) {
    serverK.verify(matcher, times);
    return this;
  }

  @Override
  public IOMockHttpServer verifyNot(Matcher1<HttpRequest> matcher) {
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

  private static BuilderK<IO<?>, IOMockHttpServer> builder(ResponseInterpreterK<IO<?>> interpreter) {
    return new BuilderK<>(Instances.monad(), interpreter) {
      @Override
      public IOMockHttpServer build() {
        return new IOMockHttpServer(buildK());
      }
    };
  }
}
