/*
 * Copyright (c) 2018-2023, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.zeromock.server;

import static com.github.tonivade.purefun.typeclasses.Instances.monad;
import static com.github.tonivade.zeromock.api.PreFilterK.filter;
import static com.github.tonivade.zeromock.server.ResponseInterpreterK.uio;
import static java.util.Objects.requireNonNull;

import com.github.tonivade.purefun.core.Matcher1;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.effect.UIO;
import com.github.tonivade.purefun.effect.UIO_;
import com.github.tonivade.zeromock.api.HttpRequest;
import com.github.tonivade.zeromock.api.HttpRouteBuilderK;
import com.github.tonivade.zeromock.api.HttpUIOService;
import com.github.tonivade.zeromock.api.PostFilter;
import com.github.tonivade.zeromock.api.PreFilter;
import com.github.tonivade.zeromock.api.UIOPostFilter;
import com.github.tonivade.zeromock.api.UIOPreFilter;
import com.github.tonivade.zeromock.api.UIORequestHandler;
import com.github.tonivade.zeromock.server.MockHttpServerK.BuilderK;

public final class UIOMockHttpServer implements HttpServer, HttpRouteBuilderK<UIO_, UIOMockHttpServer> {

  private final MockHttpServerK<UIO_> serverK;

  public UIOMockHttpServer(com.sun.net.httpserver.HttpServer server) {
    this(new MockHttpServerK<>(server, monad(UIO_.class), uio()));
  }

  private UIOMockHttpServer(MockHttpServerK<UIO_> serverK) {
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

  public static BuilderK<UIO_, UIOMockHttpServer> builder() {
    return builder(uio());
  }

  public static UIOMockHttpServer listenAt(int port) {
    return builder().port(port).build();
  }

  public static UIOMockHttpServer listenAt(String host, int port) {
    return builder().host(host).port(port).build();
  }

  public UIOMockHttpServer mount(String path, HttpUIOService other) {
    serverK.mount(path, other.build());
    return this;
  }

  public UIOMockHttpServer exec(UIORequestHandler handler) {
    serverK.exec(handler);
    return this;
  }

  public ThenStepK<UIO_, UIOMockHttpServer> preFilter(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(monad(UIO_.class), handler -> addPreFilter(matcher, handler::apply));
  }

  public UIOMockHttpServer preFilter(PreFilter filter) {
    return preFilter(filter.andThen(UIO::pure)::apply);
  }

  public UIOMockHttpServer preFilter(UIOPreFilter filter) {
    serverK.preFilter(filter);
    return this;
  }

  public UIOMockHttpServer postFilter(PostFilter filter) {
    return postFilter(filter.andThen(UIO::pure)::apply);
  }

  public UIOMockHttpServer postFilter(UIOPostFilter filter) {
    serverK.postFilter(filter);
    return this;
  }

  public UIOMockHttpServer addMapping(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    serverK.addMapping(matcher, handler);
    return this;
  }

  public UIOMockHttpServer addPreFilter(Matcher1<HttpRequest> matcher, UIORequestHandler handler) {
    serverK.preFilter(filter(monad(UIO_.class), matcher, handler));
    return this;
  }

  @Override
  public ThenStepK<UIO_, UIOMockHttpServer> when(Matcher1<HttpRequest> matcher) {
    return new ThenStepK<>(monad(UIO_.class), handler -> addMapping(matcher, handler::apply));
  }

  @Override
  public UIOMockHttpServer start() {
    serverK.start();
    return this;
  }

  @Override
  public void stop() {
    serverK.stop();
  }

  @Override
  public UIOMockHttpServer verify(Matcher1<HttpRequest> matcher) {
    serverK.verify(matcher);
    return this;
  }

  @Override
  public UIOMockHttpServer verifyNot(Matcher1<HttpRequest> matcher) {
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

  private static BuilderK<UIO_, UIOMockHttpServer> builder(ResponseInterpreterK<UIO_> interpreter) {
    return new BuilderK<>(monad(UIO_.class), interpreter) {
      @Override
      public UIOMockHttpServer build() {
        return new UIOMockHttpServer(buildK());
      }
    };
  }
}
